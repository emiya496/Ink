<template>
  <div class="publish-page">
    <NavBar />
    <div class="publish-layout">
      <!-- 左侧主编辑区 -->
      <div class="editor-area">
        <div class="publish-header">
          <h2>{{ isDraftContent ? '编辑草稿' : isEdit ? '编辑作品' : '发布作品' }}</h2>
          <el-tag v-if="isDraftContent" type="warning" size="small" style="margin-left:8px">草稿</el-tag>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <!-- 配图（封面图，最多9张） -->
          <el-form-item>
            <template #label>
              配图（最多9张，第一张为封面展示，点击可预览大图）<span style="font-size:12px;color:#bbb;margin-left:8px">每张图片大小不超过 10 MB</span>
            </template>
            <div class="cover-gallery">
              <div
                v-for="(img, idx) in form.coverImages"
                :key="idx"
                class="cover-thumb"
                @click="previewImage(img)"
              >
                <img :src="img" alt="封面" />
                <div v-if="idx === 0" class="cover-badge">封面</div>
                <el-icon class="cover-remove" @click.stop="removeCoverImage(idx)"><CircleClose /></el-icon>
              </div>
              <div
                v-if="form.coverImages.length < 9"
                class="cover-add"
                @click="triggerCoverFile"
              >
                <el-icon v-if="!coverUploading"><Plus /></el-icon>
                <el-icon v-else class="is-loading"><Loading /></el-icon>
                <span>{{ coverUploading ? '上传中...' : '上传图片' }}</span>
              </div>
              <div
                v-if="form.coverImages.length < 9"
                class="cover-add cover-add-url"
                @click="addCoverByUrl"
              >
                <el-icon><Link /></el-icon>
                <span>粘贴URL</span>
              </div>
              <input
                ref="coverFileInput"
                type="file"
                accept="image/*"
                style="display:none"
                @change="onCoverFileChange"
              />
            </div>
          </el-form-item>

          <!-- 类型选择 -->
          <el-form-item label="内容类型" prop="type">
            <el-radio-group v-model="form.type" size="large">
              <el-radio-button v-for="t in types" :key="t" :value="t">{{ t }}</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <!-- 标题 -->
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入标题（最多200字）" maxlength="200" show-word-limit size="large" />
          </el-form-item>

          <!-- 标签选择 -->
          <el-form-item label="标签（最多5个）">
            <div class="tag-selector">
              <div class="selected-tags">
                <el-tag
                  v-for="tag in selectedTags"
                  :key="tag.id"
                  closable
                  @close="removeTag(tag)"
                  class="selected-tag"
                >#{{ tag.tagName }}</el-tag>
              </div>
              <div class="tag-actions">
                <el-select
                  v-model="tagSearchVal"
                  filterable
                  allow-create
                  placeholder="搜索或创建标签"
                  @change="addTag"
                  :disabled="selectedTags.length >= 5"
                  style="width: 240px"
                >
                  <el-option v-for="t in availableTags" :key="t.id" :label="t.tagName" :value="t.id" />
                </el-select>
              </div>
            </div>
          </el-form-item>

          <!-- 编辑器（非小说类型） -->
          <el-form-item v-if="!isNovel" label="正文内容" prop="content">
            <div class="editor-container" @click="focusEditor($event, editorRef)">
              <Toolbar :editor="editorRef" :defaultConfig="toolbarConfig" />
              <Editor
                v-model="form.content"
                :defaultConfig="editorConfig"
                @onCreated="handleCreated"
                @onBlur="onMainEditorBlur"
                @onFocus="onMainEditorFocus"
                class="wangeditor"
              />
            </div>
          </el-form-item>

          <!-- 小说简介 -->
          <el-form-item v-if="isNovel" label="小说简介（可选）">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="4"
              placeholder="请输入小说简介，吸引读者..."
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>

          <!-- 小说章节管理（选择小说类型后立即显示） -->
          <div v-if="isNovel" class="chapter-section">
            <el-divider content-position="left">章节管理</el-divider>

            <div class="chapter-list">
              <!-- 渲染 1..maxOrder 的所有章节槽位，缺章显示补写 -->
              <template v-for="order in maxOrder" :key="order">
                <div v-if="getChapterByOrder(order)" class="chapter-item">
                  <span class="chapter-index">第{{ order }}章</span>
                  <span class="chapter-title-text">{{ getChapterByOrder(order).chapterTitle || '（无标题）' }}</span>
                  <div class="chapter-item-actions">
                    <el-button size="small" plain @click="openEditChapter(getChapterByOrder(order))">编辑</el-button>
                    <el-button size="small" type="danger" plain @click="deleteChapterItem(getChapterByOrder(order))">删除</el-button>
                  </div>
                </div>
                <div v-else class="chapter-item chapter-item-gap">
                  <span class="chapter-index">第{{ order }}章</span>
                  <span class="chapter-title-text chapter-gap-label">（未写）</span>
                  <div class="chapter-item-actions">
                    <el-button size="small" type="warning" plain @click="openAddChapter(order)">补写</el-button>
                  </div>
                </div>
              </template>

              <el-empty v-if="maxOrder === 0" description="还没有章节，点击下方按钮开始写第一章" :image-size="60" />
            </div>

            <div style="margin-top:12px;display:flex;align-items:center;gap:10px">
              <el-tooltip
                :content="gapOrders.length > 0 ? `请先补写第${gapOrders[0]}章，再新增下一章` : ''"
                :disabled="canAddNext"
              >
                <span>
                  <el-button type="primary" plain :disabled="!canAddNext" @click="openAddChapter(nextChapterOrder)">
                    + 新增第{{ nextChapterOrder }}章
                  </el-button>
                </span>
              </el-tooltip>
              <el-tag v-if="!isEdit && localChapters.length > 0" type="info" size="small">
                已暂存 {{ localChapters.length }} 章，发布后同步保存
              </el-tag>
            </div>
          </div>

          <!-- 可见性设置 -->
          <el-form-item label="可见性">
            <el-radio-group v-model="form.visibility">
              <el-radio-button value="public">公开</el-radio-button>
              <el-radio-button value="followers_only">仅粉丝可见</el-radio-button>
              <el-radio-button value="private">私密</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <div class="form-actions">
            <!-- 新建或草稿编辑时显示保存草稿按钮 -->
            <el-button
              v-if="!isEdit || isDraftContent"
              :loading="savingDraft"
              @click="saveDraftToBackend"
            >保存草稿</el-button>
            <el-button @click="router.back()">取消</el-button>
            <el-button type="primary" size="large" @click="submit" :loading="submitting">
              {{ isEdit && !isDraftContent ? '保存修改' : '发布作品' }}
            </el-button>
          </div>
        </el-form>
      </div>

      <!-- 右侧 AI 助手面板 -->
      <div class="ai-panel">
        <div class="ai-panel-header">
          <el-icon><MagicStick /></el-icon>
          <span>AI 写作助手</span>
        </div>

        <el-tabs v-model="aiTab" class="ai-tabs">
          <el-tab-pane label="AI续写" name="generate">
            <p class="ai-hint">基于当前内容，AI将为你续写</p>
            <el-button type="primary" :loading="aiLoading" @click="aiGenerate" style="width:100%">
              开始续写
            </el-button>
          </el-tab-pane>
          <el-tab-pane label="AI润色" name="polish">
            <p class="ai-hint">AI将优化你的文字表达。<br/>在编辑器中<b>选中文本</b>后点击，将只润色选中部分；未选中则润色全文。</p>
            <el-button type="success" :loading="aiLoading" @click="aiPolish" style="width:100%">
              开始润色
            </el-button>
          </el-tab-pane>
        </el-tabs>

        <div v-if="aiResult" class="ai-result">
          <div class="ai-result-header">
            <div style="display:flex;align-items:center;gap:6px">
              <span>AI 结果</span>
              <el-tag v-if="polishMode === 'selection'" size="small" type="success">选中文本</el-tag>
              <el-tag v-else-if="aiTab === 'polish'" size="small" type="info">全文</el-tag>
            </div>
            <div>
              <el-button size="small" type="primary" @click="insertAiResult">
                {{ polishMode === 'selection' ? '替换选中文本' : '插入到编辑器' }}
              </el-button>
              <el-button size="small" @click="aiResult = ''">清除</el-button>
            </div>
          </div>
          <div class="ai-result-content">{{ aiResult }}</div>
        </div>

        <el-empty v-else description="点击按钮，AI将帮助你创作" :image-size="80" />

        <!-- 撤销AI插入 -->
        <div v-if="lastAiInserted" class="undo-bar">
          <span>AI内容已插入到编辑器</span>
          <el-button size="small" type="warning" @click="undoAiInsert">撤销</el-button>
        </div>
      </div>
    </div>
  </div>

  <!-- 章节编辑对话框 -->
  <el-dialog
    v-model="showAddChapter"
    :title="editingChapter ? `编辑 · 第${chapterOrderForDialog}章` : `${isGapFill ? '补写' : '新增'} · 第${chapterOrderForDialog}章`"
    width="750px"
    top="4vh"
    destroy-on-close
    class="chapter-dialog"
  >
    <el-form label-position="top">
      <el-form-item label="章节标题（可选）">
        <el-input v-model="newChapter.title" placeholder="例如：初遇" />
      </el-form-item>
      <el-form-item label="章节内容">
        <div class="editor-container" @click="focusEditor($event, chapterEditorRef)">
          <Toolbar :editor="chapterEditorRef" :defaultConfig="toolbarConfig" />
          <Editor
            v-model="newChapter.content"
            :defaultConfig="editorConfig"
            @onCreated="(e: any) => chapterEditorRef = e"
            @onBlur="onChapterEditorBlur"
            @onFocus="onChapterEditorFocus"
            style="height: 360px"
          />
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showAddChapter = false">取消</el-button>
      <el-button type="primary" @click="submitChapter">保存章节</el-button>
    </template>
  </el-dialog>

  <!-- 图片预览 -->
  <el-dialog v-model="previewVisible" width="auto" :show-close="true" align-center>
    <img :src="previewUrl" style="max-width:80vw;max-height:80vh;display:block;border-radius:4px" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, shallowRef, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import NavBar from '@/components/NavBar.vue'
import { contentApi, chapterApi } from '@/api/content'
import { tagApi } from '@/api/tag'
import { aiApi } from '@/api/ai'
import { uploadApi } from '@/api/upload'

const router = useRouter()
const route = useRoute()
const isEdit = computed(() => !!route.params.id)

const form = ref({
  type: '散文',
  title: '',
  content: '',
  coverImages: [] as string[],
  tagIds: [] as number[],
  customTags: [] as string[],
  visibility: 'public',
})

const rules = {
  type: [{ required: true, message: '请选择类型' }],
  title: [{ required: true, message: '请输入标题' }],
}

const types = ['小说', '散文', '诗词', '随笔', '名人名言', '杂谈']
const formRef = ref()
const submitting = ref(false)
const savingDraft = ref(false)
const isDraftContent = ref(false)  // 当前编辑的是否是草稿

// 封面多图上传
const coverFileInput = ref<HTMLInputElement | null>(null)
const coverUploading = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')

const triggerCoverFile = () => coverFileInput.value?.click()

const previewImage = (url: string) => {
  previewUrl.value = url
  previewVisible.value = true
}

const removeCoverImage = (idx: number) => {
  form.value.coverImages.splice(idx, 1)
}

const onCoverFileChange = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (form.value.coverImages.length >= 9) {
    ElMessage.warning('最多上传9张图片')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 10 MB，请压缩后重试')
    if (coverFileInput.value) coverFileInput.value.value = ''
    return
  }
  coverUploading.value = true
  try {
    const res = await uploadApi.image(file)
    form.value.coverImages.push(res.data)
  } finally {
    coverUploading.value = false
    if (coverFileInput.value) coverFileInput.value.value = ''
  }
}

const addCoverByUrl = async () => {
  if (form.value.coverImages.length >= 9) {
    ElMessage.warning('最多上传9张图片')
    return
  }
  const { value } = await ElMessageBox.prompt('请输入图片 URL', '粘贴图片链接', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputPlaceholder: 'https://...',
    inputValidator: (v) => /^https?:\/\/.+/.test(v?.trim()) || '请输入有效的图片 URL',
  }).catch(() => ({ value: null }))
  if (value) form.value.coverImages.push(value.trim())
}

// 标签
const allTags = ref<any[]>([])
const selectedTags = ref<any[]>([])
const tagSearchVal = ref<any>(null)
const availableTags = ref<any[]>([])

const loadTags = async () => {
  const res = await tagApi.list()
  allTags.value = res.data
  availableTags.value = res.data
}

const addTag = (val: any) => {
  if (selectedTags.value.length >= 5) return
  tagSearchVal.value = null
  if (typeof val === 'string') {
    if (!selectedTags.value.find(t => t.tagName === val)) {
      selectedTags.value.push({ id: `custom_${val}`, tagName: val, isCustom: true })
    }
  } else {
    const tag = allTags.value.find(t => t.id === val)
    if (tag && !selectedTags.value.find(t => t.id === tag.id)) {
      selectedTags.value.push(tag)
    }
  }
}

const removeTag = (tag: any) => {
  selectedTags.value = selectedTags.value.filter(t => t.id !== tag.id)
}

// WangEditor
const editorRef = shallowRef()
const chapterEditorRef = shallowRef()
const toolbarConfig = {
  excludeKeys: [
    'uploadImage', 'insertImage', 'group-image',
    'uploadVideo', 'insertVideo', 'group-video',
    'fullScreen',
  ]
}

const focusEditor = (e: MouseEvent, editor: any) => {
  const target = e.target as HTMLElement
  if (!target.closest('.w-e-toolbar')) {
    editor?.focus()
  }
}
const editorConfig = { placeholder: '请输入正文内容...' }
const handleCreated = (editor: any) => { editorRef.value = editor }

const savedMainMarks = ref<Record<string, any>>({})
const savedChapterMarks = ref<Record<string, any>>({})

const onMainEditorBlur = (editor: any) => {
  try { savedMainMarks.value = { ...editor.getMarks() } } catch (_) {}
}
const onMainEditorFocus = (editor: any) => {
  const marks = { ...savedMainMarks.value }
  nextTick(() => {
    try {
      Object.entries(marks).forEach(([k, v]) => {
        if (v !== undefined && v !== null) editor.addMark(k, v)
      })
    } catch (_) {}
  })
}
const onChapterEditorBlur = (editor: any) => {
  try { savedChapterMarks.value = { ...editor.getMarks() } } catch (_) {}
}
const onChapterEditorFocus = (editor: any) => {
  const marks = { ...savedChapterMarks.value }
  nextTick(() => {
    try {
      Object.entries(marks).forEach(([k, v]) => {
        if (v !== undefined && v !== null) editor.addMark(k, v)
      })
    } catch (_) {}
  })
}

// 是否小说类型
const isNovel = computed(() => form.value.type === '小说')

// ---- 章节管理 ----
// chapters：编辑模式下从后端加载的章节
const chapters = ref<any[]>([])
// localChapters：新建小说时本地暂存，发布时批量提交
const localChapters = ref<any[]>([])

// 当前活跃章节列表（编辑模式用后端，新建模式用本地）
const activeChapters = computed(() => isEdit.value ? chapters.value : localChapters.value)

// 章节最大序号
const maxOrder = computed(() =>
  activeChapters.value.length ? Math.max(...activeChapters.value.map((c: any) => c.chapterOrder)) : 0
)

// 缺失的章节序号（稳定序号，删除不重排）
const gapOrders = computed(() => {
  const has = new Set(activeChapters.value.map((c: any) => c.chapterOrder))
  const gaps: number[] = []
  for (let i = 1; i <= maxOrder.value; i++) {
    if (!has.has(i)) gaps.push(i)
  }
  return gaps
})

// 是否可新增下一章（无缺章才允许）
const canAddNext = computed(() => gapOrders.value.length === 0)
const nextChapterOrder = computed(() => maxOrder.value + 1)

// 按序号查找章节
const getChapterByOrder = (order: number) =>
  activeChapters.value.find((c: any) => c.chapterOrder === order)

// 对话框状态
const showAddChapter = ref(false)
const editingChapter = ref<any>(null)
const isGapFill = ref(false)
const chapterOrderForDialog = ref(1)
const newChapter = ref({ title: '', content: '' })

// 打开新增/补写对话框
const openAddChapter = (order: number) => {
  editingChapter.value = null
  isGapFill.value = order <= maxOrder.value
  chapterOrderForDialog.value = order
  newChapter.value = { title: '', content: '' }
  showAddChapter.value = true
}

// 打开编辑对话框
const openEditChapter = (ch: any) => {
  editingChapter.value = ch
  isGapFill.value = false
  chapterOrderForDialog.value = ch.chapterOrder
  newChapter.value = { title: ch.chapterTitle || '', content: ch.chapterContent || '' }
  showAddChapter.value = true
}

const loadChapters = async () => {
  if (!isEdit.value) return
  const res = await chapterApi.list(Number(route.params.id))
  chapters.value = res.data
}

const submitChapter = async () => {
  if (!newChapter.value.content.trim() && !newChapter.value.title.trim()) {
    ElMessage.warning('请填写章节内容')
    return
  }

  if (editingChapter.value) {
    // 编辑已有章节
    if (isEdit.value) {
      await chapterApi.update(editingChapter.value.id, {
        chapterTitle: newChapter.value.title,
        chapterContent: newChapter.value.content,
      })
      ElMessage.success('章节已更新')
      loadChapters()
    } else {
      // 本地更新
      const idx = localChapters.value.findIndex((c: any) => c.chapterOrder === editingChapter.value.chapterOrder)
      if (idx >= 0) {
        localChapters.value[idx] = {
          ...localChapters.value[idx],
          chapterTitle: newChapter.value.title,
          chapterContent: newChapter.value.content,
        }
      }
      ElMessage.success('章节已更新')
    }
  } else {
    // 新增 / 补写
    if (isEdit.value) {
      await chapterApi.add({
        contentId: Number(route.params.id),
        chapterTitle: newChapter.value.title,
        chapterContent: newChapter.value.content,
        chapterOrder: chapterOrderForDialog.value,
      })
      ElMessage.success(isGapFill.value ? '缺章已补写' : '章节已添加')
      loadChapters()
    } else {
      // 本地暂存
      localChapters.value.push({
        chapterOrder: chapterOrderForDialog.value,
        chapterTitle: newChapter.value.title,
        chapterContent: newChapter.value.content,
      })
      localChapters.value.sort((a: any, b: any) => a.chapterOrder - b.chapterOrder)
      ElMessage.success('章节已暂存，发布后自动保存')
    }
  }

  showAddChapter.value = false
  newChapter.value = { title: '', content: '' }
  editingChapter.value = null
}

const deleteChapterItem = async (ch: any) => {
  await ElMessageBox.confirm(
    `确认删除第${ch.chapterOrder}章？删除后该章节位置将变为空缺，其余章节序号不变。`,
    '删除确认', { type: 'warning' }
  )
  if (isEdit.value) {
    await chapterApi.delete(ch.id)
    ElMessage.success('已删除')
    loadChapters()
  } else {
    localChapters.value = localChapters.value.filter((c: any) => c.chapterOrder !== ch.chapterOrder)
    ElMessage.success('已删除')
  }
}

// 类型切换保护：从小说切换到其他类型时，若有本地章节则提示
watch(() => form.value.type, async (newType, oldType) => {
  // 切换到小说：清理 content 字段中可能残留的 HTML（来自富文本编辑器）
  if (newType === '小说') {
    form.value.content = stripHtml(form.value.content)
  }
  if (oldType === '小说' && newType !== '小说' && localChapters.value.length > 0) {
    try {
      await ElMessageBox.confirm(
        '切换类型将丢失已暂存的章节，确认切换？',
        '提示', { type: 'warning', confirmButtonText: '确认切换', cancelButtonText: '取消' }
      )
      localChapters.value = []
    } catch {
      // 用户取消，恢复类型
      nextTick(() => { form.value.type = '小说' })
    }
  }
})

// ---- AI 助手 ----
const aiTab = ref('generate')
const aiLoading = ref(false)
const aiResult = ref('')
const polishMode = ref<'selection' | 'all'>('all')
const polishSelection = ref('')
const savedEditorSelection = ref<any>(null)
const lastAiInserted = ref(false)

const getEditorText = () => editorRef.value?.getText() || form.value.content?.replace(/<[^>]+>/g, '') || ''

const aiGenerate = async () => {
  const text = getEditorText()
  if (!text) { ElMessage.warning('请先输入一些内容'); return }
  aiLoading.value = true
  try {
    const res = await aiApi.generate(text)
    aiResult.value = res.data
  } finally {
    aiLoading.value = false
  }
}

const aiPolish = async () => {
  const editor = editorRef.value
  if (!editor) { ElMessage.warning('编辑器未就绪'); return }

  const selectedText: string = editor.getSelectionText ? editor.getSelectionText() : ''
  if (selectedText && selectedText.trim()) {
    polishMode.value = 'selection'
    polishSelection.value = selectedText.trim()
    savedEditorSelection.value = editor.selection
      ? JSON.parse(JSON.stringify(editor.selection))
      : null
  } else {
    const text = getEditorText()
    if (!text.trim()) { ElMessage.warning('请先输入一些内容'); return }
    polishMode.value = 'all'
    polishSelection.value = text
    savedEditorSelection.value = null
  }

  aiLoading.value = true
  try {
    const res = await aiApi.polish(polishSelection.value)
    aiResult.value = res.data
  } finally {
    aiLoading.value = false
  }
}

const insertAiResult = async () => {
  const editor = editorRef.value
  if (!editor || !aiResult.value) return

  editor.focus()
  await nextTick()

  if (polishMode.value === 'selection' && savedEditorSelection.value) {
    try {
      editor.apply({
        type: 'set_selection',
        properties: editor.selection,
        newProperties: savedEditorSelection.value,
      })
      await nextTick()
      editor.insertText(aiResult.value)
    } catch {
      editor.insertText('\n\n' + aiResult.value)
    }
  } else {
    editor.insertText('\n\n' + aiResult.value)
  }

  aiResult.value = ''
  polishMode.value = 'all'
  polishSelection.value = ''
  savedEditorSelection.value = null
  lastAiInserted.value = true
  ElMessage.success('已插入到编辑器')
}

const undoAiInsert = () => {
  editorRef.value?.undo()
  lastAiInserted.value = false
  ElMessage.success('已撤销AI插入')
}

// 草稿相关
const buildRequestData = () => {
  const tagIds = selectedTags.value.filter(t => !t.isCustom).map(t => t.id)
  const customTags = selectedTags.value.filter(t => t.isCustom).map(t => t.tagName)
  return {
    type: form.value.type,
    title: form.value.title,
    content: form.value.content,
    coverImage: form.value.coverImages.join(','),
    tagIds,
    customTags,
    visibility: form.value.visibility,
  }
}

const saveDraftToBackend = async () => {
  if (!form.value.title.trim()) { ElMessage.warning('请先输入标题再保存草稿'); return }
  savingDraft.value = true
  try {
    const data = { ...buildRequestData(), isDraft: true }
    if (isEdit.value) {
      // 已存在的草稿，直接更新
      await contentApi.update(Number(route.params.id), data)
      ElMessage.success('草稿已保存')
    } else {
      // 新建草稿，保存后跳转到编辑模式
      const res = await contentApi.create(data)
      const newContentId = res.data
      // 新建小说：同时保存本地暂存的章节
      if (isNovel.value && localChapters.value.length > 0) {
        for (const ch of localChapters.value) {
          await chapterApi.add({
            contentId: newContentId,
            chapterTitle: ch.chapterTitle,
            chapterContent: ch.chapterContent,
            chapterOrder: ch.chapterOrder,
          })
        }
      }
      ElMessage.success('草稿已保存')
      router.push(`/publish/${newContentId}`)
    }
  } finally {
    savingDraft.value = false
  }
}

// 提交（发布）
const submit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const data = buildRequestData()

    if (isEdit.value) {
      await contentApi.update(Number(route.params.id), data)
      if (isDraftContent.value) {
        ElMessage.success('发布成功！')
        router.push(`/detail/${route.params.id}`)
      } else {
        ElMessage.success('修改成功')
      }
    } else {
      const res = await contentApi.create(data)
      const newContentId = res.data

      // 新建小说：批量提交本地暂存的章节
      if (isNovel.value && localChapters.value.length > 0) {
        for (const ch of localChapters.value) {
          await chapterApi.add({
            contentId: newContentId,
            chapterTitle: ch.chapterTitle,
            chapterContent: ch.chapterContent,
            chapterOrder: ch.chapterOrder,
          })
        }
      }

      ElMessage.success('发布成功')
      if (isNovel.value) {
        router.push(`/publish/${newContentId}`)
      } else {
        router.push(`/detail/${newContentId}`)
      }
    }
  } finally {
    submitting.value = false
  }
}

const stripHtml = (html: string) =>
  (html || '').replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').trim()

const loadContentForEdit = async () => {
  const res = await contentApi.detail(Number(route.params.id))
  const d = res.data
  const coverImages = d.coverImage ? d.coverImage.split(',').filter((s: string) => s.trim()) : []
  // 小说类型的 content 字段存储纯文本简介，剥离历史遗留的 HTML 标签
  const content = d.type === '小说' ? stripHtml(d.content) : d.content
  form.value = { type: d.type, title: d.title, content, coverImages, tagIds: [], customTags: [], visibility: d.visibility || 'public' }
  selectedTags.value = d.tags || []
  isDraftContent.value = d.status === '草稿'
  loadChapters()
}

onMounted(async () => {
  loadTags()
  if (isEdit.value) {
    await loadContentForEdit()
  }
})

watch(() => route.params.id, async (newId) => {
  if (newId) {
    await loadContentForEdit()
  }
})

onBeforeUnmount(() => {
  editorRef.value?.destroy()
  chapterEditorRef.value?.destroy()
})
</script>

<style scoped>
.publish-page { min-height: 100vh; background: #f0f2f5; }
.publish-layout {
  max-width: 1300px;
  margin: 20px auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 20px;
}
.publish-header { display: flex; align-items: center; margin-bottom: 20px; }
.publish-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 8px;
}
.publish-header h2::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 20px;
  background: #409eff;
  border-radius: 2px;
}
.editor-area {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.editor-container {
  border: 1px solid #e4e4e4;
  border-radius: 8px;
  width: 100%;
  overflow: hidden;
}
.wangeditor { min-height: 320px; max-height: 600px; overflow-y: auto; }
:deep(.wangeditor .w-e-text-container) { min-height: 280px; max-height: 560px; overflow-y: auto; }
/* 修正 placeholder 与光标对齐 */
:deep(.w-e-text-container [data-slate-editor]) {
  border-top: none !important;
  padding-top: 10px !important;
}
:deep(.w-e-text-container [data-slate-editor] > p:first-child) {
  margin-top: 0 !important;
}
:deep(.w-e-text-placeholder) {
  top: 10px !important;
  font-style: normal !important;
  line-height: 1.5 !important;
}
.tag-selector { display: flex; flex-direction: column; gap: 10px; }
.selected-tags { display: flex; flex-wrap: wrap; gap: 8px; min-height: 32px; }
.selected-tag { cursor: default; }
.form-actions { display: flex; justify-content: flex-end; align-items: center; gap: 12px; margin-top: 20px; }

.ai-panel {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  height: fit-content;
  position: sticky;
  top: 80px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.ai-panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.ai-hint { font-size: 13px; color: #aaa; margin-bottom: 12px; }
.ai-result { margin-top: 16px; }
.ai-result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}
.ai-result-content {
  font-size: 14px;
  line-height: 1.75;
  color: #555;
  background: #f8faff;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid rgba(64, 158, 255, 0.12);
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
}

.cover-input-wrap { display: flex; flex-direction: column; gap: 10px; width: 100%; }
.cover-upload-row { display: flex; align-items: center; gap: 0; }
.cover-preview {
  width: 120px;
  height: 160px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e4e4e4;
}

/* 多图封面 */
.cover-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: flex-start;
}
.cover-thumb {
  position: relative;
  width: 90px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e4e4;
  cursor: pointer;
  flex-shrink: 0;
  transition: box-shadow 0.2s;
}
.cover-thumb:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.12); }
.cover-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: opacity 0.2s;
}
.cover-thumb:hover img { opacity: 0.75; }
.cover-badge {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(64,158,255,0.9);
  color: #fff;
  font-size: 11px;
  text-align: center;
  padding: 3px 0;
}
.cover-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  color: #fff;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
  padding: 2px;
  font-size: 16px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}
.cover-thumb:hover .cover-remove { opacity: 1; }
.cover-add {
  width: 90px;
  height: 120px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  color: #ccc;
  font-size: 12px;
  transition: border-color 0.2s, color 0.2s, background 0.2s;
  flex-shrink: 0;
}
.cover-add:hover { border-color: #409eff; color: #409eff; background: #f0f7ff; }
.cover-add-url { border-color: #c0c4cc; color: #909399; }
.cover-add-url:hover { border-color: #67c23a; color: #67c23a; background: #f0f9eb; }
.cover-add .el-icon { font-size: 24px; }

.chapter-section { margin-top: 8px; }
.chapter-list { display: flex; flex-direction: column; gap: 8px; }
.chapter-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 8px;
  background: #fafafa;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.chapter-item:hover { border-color: rgba(64, 158, 255, 0.3); box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.chapter-index { font-size: 13px; color: #bbb; white-space: nowrap; flex-shrink: 0; }
.chapter-title-text { flex: 1; font-size: 14px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.chapter-item-actions { display: flex; gap: 6px; flex-shrink: 0; }
.chapter-item-gap { background: #fffdf5; border-color: rgba(230, 162, 60, 0.2); }
.chapter-gap-label { color: #c0a060; font-style: italic; }

.undo-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 14px;
  background: #fffbf0;
  border: 1px solid #ffe8a1;
  border-radius: 8px;
  font-size: 13px;
  color: #b8860b;
  margin-top: 12px;
}
</style>

<style>
/* 章节编辑弹窗：body 可滚动，确保底部按钮始终可见 */
.chapter-dialog .el-dialog__body {
  max-height: calc(92vh - 130px);
  overflow-y: auto;
}
</style>
