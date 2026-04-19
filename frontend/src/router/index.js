import { createRouter, createWebHistory } from 'vue-router'
import SearchView from '../views/SearchView.vue'
import PaymentView from '../views/PaymentView.vue'
import RefundView from '../views/RefundView.vue'

const routes = [
  { path: '/', redirect: '/search' },
  { path: '/search', component: SearchView },
  { path: '/payments/new', component: PaymentView },
  { path: '/payments/:paymentId', component: PaymentView },
  { path: '/payments/:paymentId/refund', component: RefundView }
]

export default createRouter({ history: createWebHistory(), routes })
