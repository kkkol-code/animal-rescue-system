<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="layout__aside">
      <div class="logo">
        <el-icon :size="28"><House /></el-icon>
        <span class="logo__text">流浪动物救助站</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        :collapse="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计看板</span>
        </el-menu-item>
        <el-menu-item index="/animals">
          <el-icon><Notebook /></el-icon>
          <span>动物档案大厅</span>
        </el-menu-item>
        <el-menu-item index="/adoption">
          <el-icon><DocumentChecked /></el-icon>
          <span>领养办理与审核</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主体 -->
    <el-container>
      <el-header class="layout__header">
        <div class="header__left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header__right">
          <el-tag type="success" effect="dark" size="small">系统运行中</el-tag>
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
import { House, DataAnalysis, Notebook, DocumentChecked } from '@element-plus/icons-vue'

const route = useRoute()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')
</script>

<style scoped>
.layout {
  height: 100vh;
}

.layout__aside {
  background-color: #304156;
  overflow-y: auto;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 60px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
  height: 50px;
}

.header__right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header__user {
  color: #606266;
  font-size: 14px;
}

.layout__main {
  background: #f0f2f5;
  min-height: 0;
}
</style>
