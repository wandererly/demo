<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-title">HRM Admin 登录</div>
      <div class="login-sub">欢迎回来，请登录系统</div>
      <input class="input" v-model="form.username" placeholder="用户名" />
      <input class="input" type="password" v-model="form.password" placeholder="密码" />

      <div class="remember-row">
        <label>
          <input type="checkbox" v-model="form.remember" /> 记住我
        </label>
      </div>

      <button class="btn" @click="login">登录</button>
      <div v-if="error" class="alert">{{ error }}</div>

      <div class="divider"></div>
      <div class="login-sub">没有管理员账号？快速注册</div>
      <input class="input" v-model="register.username" placeholder="新用户名" />
      <input class="input" type="password" v-model="register.password" placeholder="新密码" />
      <button class="btn-ghost" @click="registerAdmin">注册管理员</button>
      <div v-if="registerMsg" class="badge">{{ registerMsg }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { http } from '../api/http'
import { setToken, setUser } from '../auth'

const router = useRouter()
const error = ref('')
const registerMsg = ref('')
const form = ref({ username: '', password: '', remember: true })
const register = ref({ username: '', password: '' })

const login = async () => {
  error.value = ''
  try {
    const data = await http.post('/api/auth/login', {
      username: form.value.username,
      password: form.value.password
    })
    setToken(data.token, form.value.remember)
    setUser({ username: data.username, role: data.role, permissions: data.permissions || [] }, form.value.remember)
    router.push('/')
  } catch (e) {
    error.value = e.message
  }
}

const registerAdmin = async () => {
  registerMsg.value = ''
  try {
    await http.post('/api/auth/register', {
      username: register.value.username,
      password: register.value.password
    })
    registerMsg.value = '管理员注册成功'
    register.value = { username: '', password: '' }
  } catch (e) {
    registerMsg.value = e.message
  }
}
</script>
