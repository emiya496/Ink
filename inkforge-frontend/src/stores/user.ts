import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '@/api/user'

interface UserInfo {
  id: number
  username: string
  avatar: string
  role: string
  status: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('inkforge_token') || '')
  const userInfo = ref<UserInfo | null>(null)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('inkforge_token', t)
  }

  function setUserInfo(u: UserInfo) {
    userInfo.value = u
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('inkforge_token')
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await userApi.getInfo()
      userInfo.value = res.data
    } catch (e) {
      logout()
    }
  }

  const isLoggedIn = () => !!token.value
  const isAdmin = () => userInfo.value?.role === 'admin'

  return { token, userInfo, setToken, setUserInfo, logout, fetchUserInfo, isLoggedIn, isAdmin }
})
