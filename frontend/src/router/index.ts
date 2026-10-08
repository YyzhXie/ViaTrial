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
    {
      path: '/questions/new',
      name: 'question-create',
      component: () => import('@/views/QuestionFormView.vue'),
    },
    {
      path: '/questions/:id/edit',
      name: 'question-edit',
      component: () => import('@/views/QuestionFormView.vue'),
    },
  ],
})

export default router
