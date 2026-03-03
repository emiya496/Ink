import request from './request'

export const aiApi = {
  generate: (content: string) => request.post<any, any>('/ai/generate', { content }),
  summary: (content: string) => request.post<any, any>('/ai/summary', { content }),
  polish: (content: string) => request.post<any, any>('/ai/polish', { content }),
  keywords: (content: string) => request.post<any, any>('/ai/keywords', { content }),
  sentiment: (content: string) => request.post<any, any>('/ai/sentiment', { content }),
  style: (content: string) => request.post<any, any>('/ai/style', { content }),
  qa: (content: string, question: string) => request.post<any, any>('/ai/qa', { content, question }),
}
