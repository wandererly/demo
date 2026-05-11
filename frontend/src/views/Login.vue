<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-title">HRM Admin 登录</div>
      <div class="login-sub">欢迎回来，请登录系统</div>
      <input class="input" v-model="form.username" placeholder="用户名" />
      <input class="input" type="password" v-model="form.password" placeholder="密码" />

      <div class="captcha-row">
        <input class="input" v-model="form.captchaCode" placeholder="验证码" style="flex:1" />
        <div class="captcha-box" @click="refreshCaptcha" title="点击刷新">
          <img v-if="captchaImg" :src="'data:image/png;base64,' + captchaImg" alt="captcha" />
          <span v-else style="font-size:12px;color:var(--muted)">加载中...</span>
        </div>
      </div>

      <div class="remember-row">
        <label>
          <input type="checkbox" v-model="form.remember" /> 记住我
        </label>
      </div>

      <button class="btn" :disabled="loading" @click="login">{{ loading ? '登录中...' : '登录' }}</button>
      <div v-if="error" class="alert">{{ error }}</div>

      <div class="divider"></div>
      <div class="login-sub">没有管理员账号？快速注册</div>
      <input class="input" v-model="register.username" placeholder="新用户名" />
      <input class="input" type="password" v-model="register.password" placeholder="新密码" />
      <button class="btn-ghost" :disabled="regLoading" @click="registerAdmin">{{ regLoading ? '注册中...' : '注册管理员' }}</button>
      <div v-if="registerMsg" class="badge">{{ registerMsg }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { http } from '../api/http'
import { setToken, setUser } from '../auth'

const router = useRouter()
const error = ref('')
const loading = ref(false)
const registerMsg = ref('')
const regLoading = ref(false)
const captchaImg = ref('')
const captchaId = ref('')
const form = ref({ username: '', password: '', captchaCode: '', remember: true })
const register = ref({ username: '', password: '' })

const refreshCaptcha = async () => {
  try {
    const data = await http.get('/api/auth/captcha')
    captchaId.value = data.captchaId
    captchaImg.value = data.imageBase64
  } catch (e) {
    // captcha load failed
  }
}

onMounted(refreshCaptcha)

const login = async () => {
  error.value = ''
  if (!form.value.username.trim() || !form.value.password.trim()) {
    error.value = '请输入用户名和密码'
    return
  }
  if (!form.value.captchaCode.trim()) {
    error.value = '请输入验证码'
    return
  }
  loading.value = true
  try {
    const data = await http.post('/api/auth/login', {
      username: form.value.username.trim(),
      password: form.value.password,
      captchaId: captchaId.value,
      captchaCode: form.value.captchaCode
    })
    setToken(data.token, form.value.remember)
    setUser({ username: data.username, role: data.role, permissions: data.permissions || [] }, form.value.remember)
    router.push('/')
  } catch (e) {
    error.value = e.message
    refreshCaptcha()
    form.value.captchaCode = ''
  } finally {
    loading.value = false
  }
}

const registerAdmin = async () => {
  registerMsg.value = ''
  if (!register.value.username.trim() || !register.value.password.trim()) {
    registerMsg.value = '请输入用户名和密码'
    return
  }
  regLoading.value = true
  try {
    await http.post('/api/auth/register', {
      username: register.value.username.trim(),
      password: register.value.password
    })
    registerMsg.value = '管理员注册成功，请登录'
    register.value = { username: '', password: '' }
  } catch (e) {
    registerMsg.value = e.message
  } finally {
    regLoading.value = false
  }
}
</script>
