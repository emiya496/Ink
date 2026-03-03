import request from './request'

export const adminApi = {
  // 用户管理
  listUsers: (params: any) => request.get<any, any>('/admin/users', { params }),
  updateUserStatus: (id: number, status: string) => request.put(`/admin/users/${id}/status`, { status }),
  deleteUser: (id: number) => request.delete(`/admin/users/${id}`),

  // 内容管理
  listContents: (params: any) => request.get<any, any>('/admin/contents', { params }),
  updateContentStatus: (id: number, status: string) => request.put(`/admin/contents/${id}/status`, { status }),
  deleteContent: (id: number) => request.delete(`/admin/contents/${id}`),

  // 标签管理
  listTags: (params: any) => request.get<any, any>('/admin/tags', { params }),
  createTag: (tagName: string) => request.post('/admin/tags', { tagName }),
  updateTagStatus: (id: number, status: string) => request.put(`/admin/tags/${id}/status`, { status }),
  deleteTag: (id: number) => request.delete(`/admin/tags/${id}`),

  // 评论管理
  listComments: (params: any) => request.get<any, any>('/admin/comments', { params }),
  deleteComment: (id: number) => request.delete(`/admin/comments/${id}`),

  // AI统计
  getAiStats: () => request.get<any, any>('/admin/ai/stats'),
}
