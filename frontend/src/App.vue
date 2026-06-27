<template>
  <el-config-provider>
    <el-container class="app-layout">
      <el-header class="app-header">
        <div class="brand">ViaTrial</div>
        <el-menu
          :default-active="route.path"
          mode="horizontal"
          router
          class="app-menu"
        >
          <el-menu-item index="/">题目列表</el-menu-item>
          <el-menu-item index="/paper">生成试卷</el-menu-item>
        </el-menu>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-config-provider>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
</script>
