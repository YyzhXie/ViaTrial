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
      name: 'paper-preview',
      component: () => import('@/views/PaperGenerateView.vue'),
    },
    { path: '/paper/practice', name: 'paper-practice', component: () => import('@/views/PaperGenerateView.vue') },
    { path: '/paper/result', name: 'paper-result', component: () => import('@/views/PaperGenerateView.vue') },
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
