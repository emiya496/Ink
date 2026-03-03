import { ref } from 'vue'

export const isDark = ref(false)

export function initTheme() {
  const saved = localStorage.getItem('inkforge_dark') === 'true'
  isDark.value = saved
  document.documentElement.classList.toggle('dark', saved)
}

export function toggleTheme() {
  isDark.value = !isDark.value
  document.documentElement.classList.toggle('dark', isDark.value)
  localStorage.setItem('inkforge_dark', isDark.value ? 'true' : 'false')
}
