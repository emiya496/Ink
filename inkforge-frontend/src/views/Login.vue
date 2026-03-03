<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-header">
        <img src="/logo.png" class="brand-logo-img" alt="InkForge" />
        <p class="brand-desc">文学创作社区</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 登录 -->
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" @submit.prevent="doLogin">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="用户名或邮箱" prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
            </el-form-item>
            <div class="forgot-link" @click="openForgotDialog">忘记密码？</div>
            <el-button type="primary" size="large" native-type="submit" :loading="loading" style="width:100%">登录</el-button>
          </el-form>
        </el-tab-pane>

        <!-- 注册 -->
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" @submit.prevent="doRegister">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="用户名（3-20字符）" prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="密码（6-30字符）" prefix-icon="Lock" size="large" show-password />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" size="large" show-password />
            </el-form-item>
            <el-form-item prop="email">
              <el-input v-model="registerForm.email" placeholder="邮箱（选填，可用于找回密码）" prefix-icon="Message" size="large" />
            </el-form-item>
            <p class="email-tip">绑定邮箱后可通过邮箱登录并找回密码</p>
            <el-button type="primary" size="large" native-type="submit" :loading="loading" style="width:100%">注册</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="back-home" @click="router.push('/')">← 返回首页</div>
    </div>
  </div>

  <!-- 忘记密码弹窗 -->
  <el-dialog v-model="forgotVisible" title="找回密码" width="420px" :close-on-click-modal="false">
    <el-form :model="forgotForm" label-width="90px">
      <el-form-item label="注册邮箱">
        <el-input v-model="forgotForm.email" placeholder="请输入绑定的邮箱" />
      </el-form-item>
      <el-form-item label="验证码">
        <div style="display:flex;gap:8px;width:100%">
          <el-input v-model="forgotForm.code" placeholder="6位验证码" style="flex:1" />
          <el-button
            :disabled="forgotCooldown > 0"
            :loading="sendingForgotCode"
            @click="sendForgotCode"
            style="white-space:nowrap"
          >
            {{ forgotCooldown > 0 ? `${forgotCooldown}s后重发` : '发送验证码' }}
          </el-button>
        </div>
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="forgotForm.newPassword" type="password" placeholder="6-30字符" show-password />
      </el-form-item>
      <el-form-item label="确认新密码">
        <el-input v-model="forgotForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="forgotVisible = false">取消</el-button>
      <el-button type="primary" :loading="resettingPassword" @click="confirmReset">确认重置</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref()
const registerFormRef = ref()

onMounted(() => {
  if (route.query.tab === 'register') activeTab.value = 'register'
})

const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', password: '', confirmPassword: '', email: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名或邮箱' }],
  password: [{ required: true, message: '请输入密码' }],
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名' },
    { min: 3, max: 20, message: '用户名长度3-20字符' },
  ],
  password: [
    { required: true, message: '请输入密码' },
    { min: 6, max: 30, message: '密码长度6-30字符' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码' },
    {
      validator: (_: any, value: string, callback: Function) => {
        if (value !== registerForm.value.password) callback(new Error('两次密码不一致'))
        else callback()
      }
    }
  ],
  email: [
    {
      validator: (_: any, value: string, callback: Function) => {
        if (!value) { callback(); return } // 选填，空值直接通过
        const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!emailReg.test(value)) callback(new Error('邮箱格式不正确'))
        else callback()
      }
    }
  ],
}

const doLogin = async () => {
  await loginFormRef.value?.validate()
  loading.value = true
  try {
    const res = await userApi.login(loginForm.value)
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } finally {
    loading.value = false
  }
}

const doRegister = async () => {
  await registerFormRef.value?.validate()
  loading.value = true
  try {
    await userApi.register({
      username: registerForm.value.username,
      password: registerForm.value.password,
      email: registerForm.value.email || undefined,
    })
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.value.username = registerForm.value.username
  } finally {
    loading.value = false
  }
}

// 忘记密码
const forgotVisible = ref(false)
const forgotForm = ref({ email: '', code: '', newPassword: '', confirmPassword: '' })
const forgotCooldown = ref(0)
const sendingForgotCode = ref(false)
const resettingPassword = ref(false)
let forgotTimer: ReturnType<typeof setInterval> | null = null

const openForgotDialog = () => {
  forgotForm.value = { email: '', code: '', newPassword: '', confirmPassword: '' }
  forgotCooldown.value = 0
  forgotVisible.value = true
}

const sendForgotCode = async () => {
  if (!forgotForm.value.email) {
    ElMessage.warning('请先填写邮箱地址')
    return
  }
  sendingForgotCode.value = true
  try {
    await userApi.sendPasswordResetCode({ email: forgotForm.value.email })
    ElMessage.success('验证码已发送，请查收邮件')
    forgotCooldown.value = 60
    forgotTimer = setInterval(() => {
      forgotCooldown.value--
      if (forgotCooldown.value <= 0 && forgotTimer) {
        clearInterval(forgotTimer)
        forgotTimer = null
      }
    }, 1000)
  } finally {
    sendingForgotCode.value = false
  }
}

const confirmReset = async () => {
  if (!forgotForm.value.email || !forgotForm.value.code || !forgotForm.value.newPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (forgotForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  if (forgotForm.value.newPassword !== forgotForm.value.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  resettingPassword.value = true
  try {
    await userApi.resetPassword({
      email: forgotForm.value.email,
      code: forgotForm.value.code,
      newPassword: forgotForm.value.newPassword,
    })
    ElMessage.success('密码重置成功，请重新登录')
    forgotVisible.value = false
  } finally {
    resettingPassword.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: url('@/assets/login-bg.png') center center / cover no-repeat;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-box {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  width: 400px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.login-header {
  text-align: center;
  margin-bottom: 24px;
}
.brand-logo-img {
  width: 180px;
  height: auto;
  margin-bottom: 8px;
}
.brand-desc { color: #999; font-size: 14px; }
.login-tabs { margin-bottom: 8px; }
.forgot-link {
  text-align: right;
  font-size: 13px;
  color: #409eff;
  cursor: pointer;
  margin-bottom: 12px;
  margin-top: -4px;
}
.forgot-link:hover { text-decoration: underline; }
.email-tip {
  font-size: 12px;
  color: #f0a020;
  margin: -8px 0 12px;
  line-height: 1.4;
}
.back-home {
  text-align: center;
  margin-top: 20px;
  color: #999;
  cursor: pointer;
  font-size: 14px;
}
.back-home:hover { color: #409eff; }
</style>
