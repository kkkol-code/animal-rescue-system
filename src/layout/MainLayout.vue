<template>
  <el-container class="layout">
    <el-aside width="220px" class="layout__aside">
      <div class="logo">
        <span class="logo__icon">🐱</span>
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
          <span class="menu-icon">📊</span>
          <span>数据统计看板</span>
        </el-menu-item>
        <el-menu-item index="/animals">
          <span class="menu-icon">🐕</span>
          <span>动物档案大厅</span>
        </el-menu-item>
        <el-menu-item index="/adoption">
          <span class="menu-icon">🏠</span>
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
          <div class="header__avatar">管</div>
          <span class="header__user">管理员</span>
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

const route = useRoute()
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')
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
.logo__icon { font-size: 28px; }

.menu-icon { margin-right: 6px; font-size: 18px; }

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
.header__avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: linear-gradient(135deg, #98D8A8, #6CB880);
  color: #fff; font-size: 13px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.header__user { color: #8C8C8C; font-size: 13px; }

.layout__main {
  background: #FFFAF5;
  min-height: 0;
}
</style>
