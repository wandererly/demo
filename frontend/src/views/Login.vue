<template>
  <div class="login-page">
    <div class="login-backdrop">
      <span class="auth-glow auth-glow-a"></span>
      <span class="auth-glow auth-glow-b"></span>
      <span class="auth-grid"></span>
    </div>

    <section class="auth-panel">
      <div class="auth-intro">
        <div class="auth-mark" aria-label="HRM">
          <span class="auth-mark-core"></span>
          <span class="auth-mark-orbit auth-mark-orbit-a"></span>
          <span class="auth-mark-orbit auth-mark-orbit-b"></span>
        </div>
        <p class="auth-kicker">Enterprise Workforce Platform</p>
        <h1>人事一体化控制台</h1>
        <p class="auth-copy">面向企业 HR 管理场景，集中处理员工档案、考勤请假、绩效评审、薪酬生成和审批协作。</p>
      </div>

      <div class="login-card">
        <div class="auth-tabs">
          <button :class="{ active: mode === 'login' }" @click="switchMode('login')">登录</button>
          <button :class="{ active: mode === 'register' }" @click="switchMode('register')">注册</button>
        </div>

        <div v-if="mode === 'login'" class="auth-form">
          <div>
            <div class="login-title">管理员登录</div>
            <div class="login-sub">输入账号、密码和验证码进入系统</div>
          </div>

          <input class="input auth-input" v-model="form.username" placeholder="用户名" autocomplete="username" />
          <div class="password-field">
            <input
              class="input auth-input"
              :type="showLoginPassword ? 'text' : 'password'"
              v-model="form.password"
              placeholder="密码"
              autocomplete="current-password"
            />
            <button type="button" @click="showLoginPassword = !showLoginPassword">{{ showLoginPassword ? '隐藏' : '显示' }}</button>
          </div>

          <div class="captcha-row">
            <input class="input auth-input" v-model="form.captchaCode" placeholder="验证码" />
            <button class="captcha-box" type="button" @click="refreshCaptcha" title="点击刷新">
              <img v-if="captchaImg" :src="'data:image/png;base64,' + captchaImg" alt="captcha" />
              <span v-else>{{ captchaLoading ? '刷新中' : '加载中' }}</span>
            </button>
          </div>

          <div class="remember-row">
            <label>
              <input type="checkbox" v-model="form.remember" /> 记住我
            </label>
            <button class="text-button" type="button" @click="refreshCaptcha">刷新验证码</button>
          </div>

          <button class="btn auth-submit" :disabled="loading" @click="login">
            <span v-if="loading" class="button-spinner"></span>
            {{ loading ? '登录中...' : '登录系统' }}
          </button>
          <div v-if="error" class="alert auth-alert">{{ error }}</div>
        </div>

        <div v-else class="auth-form">
          <div>
            <div class="login-title">注册管理员</div>
            <div class="login-sub">创建账号后自动返回登录面板</div>
          </div>

          <input class="input auth-input" v-model="register.username" placeholder="新用户名" autocomplete="username" />
          <div class="password-field">
            <input
              class="input auth-input"
              :type="showRegisterPassword ? 'text' : 'password'"
              v-model="register.password"
              placeholder="新密码"
              autocomplete="new-password"
            />
            <button type="button" @click="showRegisterPassword = !showRegisterPassword">{{ showRegisterPassword ? '隐藏' : '显示' }}</button>
          </div>

          <button class="btn-ghost auth-register" :disabled="regLoading" @click="registerAdmin">
            <span v-if="regLoading" class="button-spinner dark"></span>
            {{ regLoading ? '注册中...' : '注册管理员' }}
          </button>
          <div v-if="registerMsg" class="badge auth-message">{{ registerMsg }}</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { http } from '../api/http'
import { setToken, setUser } from '../auth'

const router = useRouter()
const mode = ref('login')
const error = ref('')
const loading = ref(false)
const registerMsg = ref('')
const regLoading = ref(false)
const captchaLoading = ref(false)
const captchaImg = ref('')
const captchaId = ref('')
const showLoginPassword = ref(false)
const showRegisterPassword = ref(false)
const form = ref({ username: '', password: '', captchaCode: '', remember: true })
const register = ref({ username: '', password: '' })

const switchMode = (nextMode) => {
  mode.value = nextMode
  error.value = ''
  registerMsg.value = ''
}

const refreshCaptcha = async () => {
  captchaLoading.value = true
  try {
    const data = await http.get('/api/auth/captcha')
    captchaId.value = data.captchaId
    captchaImg.value = data.imageBase64
  } catch (e) {
    error.value = mode.value === 'login' ? '验证码加载失败，请稍后重试' : error.value
  } finally {
    captchaLoading.value = false
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
    setUser({ username: data.username, role: data.role, empId: data.empId || null, permissions: data.permissions || [] }, form.value.remember)
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
    setTimeout(() => switchMode('login'), 700)
  } catch (e) {
    registerMsg.value = e.message
  } finally {
    regLoading.value = false
  }
}
</script>
