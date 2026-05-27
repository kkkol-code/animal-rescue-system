<template>
  <el-container class="layout">
    <el-aside width="220px" class="layout__aside">
      <div class="logo">
        <img class="logo__icon" src="@/assets/rescue_cat.png" alt="logo" />
        <span class="logo__text">流浪动物救助站</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="transparent"
        text-color="#7A8B6E"
        active-text-color="#5EA87E"
      >
        <el-menu-item index="/dashboard">
          <img class="menu-icon" src="@/assets/icon_dashboard.svg" alt="dashboard" />
          <span>数据统计看板</span>
        </el-menu-item>
        <el-menu-item index="/animals">
          <img class="menu-icon" src="@/assets/rescue_dog.png" alt="animals" />
          <span>动物档案大厅</span>
        </el-menu-item>
        <el-menu-item index="/adoption">
          <img class="menu-icon" src="@/assets/icon_adoption.svg" alt="adoption" />
          <span>领养办理与审核</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <span>🫶 每一只都值得爱</span>
      </div>
    </el-aside>

    <el-container>
      <el-header class="layout__header">
        <div class="header__left">
          <span class="header__brand">🐾 流浪动物救助站</span>
          <span class="header__subtitle">全流程管理系统</span>
          <el-divider direction="vertical" />
          <el-breadcrumb separator="🐾">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header__right">
          <el-tag type="success" effect="plain" round size="small">🟢 运行中</el-tag>
          <div class="header__role-area" @click="handleSwitchRole" title="点击切换账号（演示用）">
            <div class="header__avatar">{{ role === 'owner' ? '主' : '管' }}</div>
            <span class="header__user">{{ role === 'owner' ? '宠物主人' : '管理员' }}</span>
            <span class="header__switch-icon">🔄</span>
          </div>
        </div>
      </el-header>
      <el-main class="layout__main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { login } from '@/api/admin'

const route = useRoute()
const role = sessionStorage.getItem('role') || 'admin'
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')

const handleSwitchRole = () => {
  const isOwner = sessionStorage.getItem('role') === 'owner'
  const current = isOwner ? '宠物主人' : '管理员'
  const target = isOwner ? '管理员' : '宠物主人'

  if (isOwner) {
    // 切换到管理员：需要输入密码
    ElMessageBox.prompt('请输入管理员密码以切换到后台', '切换为管理员', {
      confirmButtonText: '确认切换',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPlaceholder: '请输入管理员密码'
    }).then(async ({ value }) => {
      try {
        const savedUser = sessionStorage.getItem('username') || 'admin'
        await login({ username: savedUser, password: value })
        sessionStorage.setItem('role', 'admin')
        ElMessage.success('已切换为管理员')
        window.location.reload()
      } catch {
        ElMessage.error('密码错误，切换失败')
      }
    }).catch(() => {})
  } else {
    // 切换到宠物主人：直接切换
    ElMessageBox.confirm(
      '当前为管理员模式，是否切换为宠物主人？',
      '切换账号',
      { confirmButtonText: '确认切换', cancelButtonText: '取消', type: 'info' }
    ).then(() => {
      sessionStorage.setItem('role', 'owner')
      window.location.reload()
    }).catch(() => {})
  }
}
</script>

<style scoped>
.layout { height: 100vh; }

.layout__aside {
  background: linear-gradient(180deg, #FFF8F0 0%, #F0F8F0 100%);
  border-right: 1px solid #F0E8DC;
  display: flex;
  flex-direction: column;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 64px;
  font-size: 16px;
  font-weight: 700;
  color: #5EA87E;
  border-bottom: 1px solid #F0E8DC;
}
.logo__icon { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; }

.menu-icon { width: 24px; height: 24px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }

.sidebar-footer {
  margin-top: auto;
  padding: 16px;
  text-align: center;
  font-size: 11px;
  color: #C0B8A8;
  border-top: 1px solid #F0E8DC;
}

.layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #F0E8DC;
  padding: 0 24px;
  height: 54px;
}
.header__left { display: flex; align-items: center; gap: 10px; }
.header__brand { font-size: 15px; font-weight: 800; color: #5EA87E; }
.header__subtitle { font-size: 11px; color: #C8C0B0; }
.header__right { display: flex; align-items: center; gap: 10px; }
.header__role-area {
  display: flex; align-items: center; gap: 8px;
  cursor: pointer; padding: 4px 10px; border-radius: 20px;
  transition: background 0.2s;
}
.header__role-area:hover { background: rgba(152,216,168,0.12); }

.header__avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: linear-gradient(135deg, #98D8A8, #6CB880);
  color: #fff; font-size: 13px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.header__user { color: #8C8C8C; font-size: 13px; }

.header__switch-icon {
  font-size: 14px; opacity: 0;
  transition: opacity 0.2s;
}
.header__role-area:hover .header__switch-icon { opacity: 1; }

.layout__main {
  background: #FFFAF5;
  min-height: 0;
}
</style>
