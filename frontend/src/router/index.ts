import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'questions',
      component: () => import('@/views/QuestionListView.vue'),
    },
    {
      path: '/paper',
      name: 'paper',
      component: () => import('@/views/PaperGenerateView.vue'),
    },
  ],
})

export default router
