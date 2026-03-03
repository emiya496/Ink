import request from './request'

export interface RegisterData { username: string; password: string; email?: string }
export interface LoginData { username: string; password: string }

export const userApi = {
  register: (data: RegisterData) => request.post('/user/register', data),
  login: (data: LoginData) => request.post<any, any>('/user/login', data),
  getInfo: () => request.get<any, any>('/user/info'),
  updateInfo: (data: { avatar: string }) => request.put('/user/info', data),
  sendEmailCode: (data: { email: string; purpose: string }) => request.post('/user/email/send-code', data),
  bindEmail: (data: { email: string; code: string }) => request.post('/user/email/bind', data),
  changeEmail: (data: { email: string; code: string }) => request.put('/user/email/change', data),
  sendPasswordResetCode: (data: { email: string }) => request.post('/user/password/send-code', data),
  resetPassword: (data: { email: string; code: string; newPassword: string }) => request.post('/user/password/reset', data),
  updateUsername: (data: { newUsername: string }) => request.put('/user/username', data),
  changePassword: (data: { oldPassword: string; newPassword: string }) => request.put('/user/password', data),
  sendDeleteCode: () => request.post('/user/account/send-delete-code'),
  deleteAccount: (data: { password: string; code: string }) => request.delete('/user/account', { data }),
}

