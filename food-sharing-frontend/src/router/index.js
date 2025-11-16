import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import CreatePost from '@/views/CreatePost.vue'
import PostDetail from '@/views/PostDetail.vue'
import Profile from '@/views/Profile.vue'
import MyFavorites from '@/views/MyFavorites.vue'

const routes = [
  { path: '/', name: 'home', component: Home },
  { path: '/login', name: 'login', component: Login },
  { path: '/register', name: 'register', component: Register },
  { 
    path: '/create', 
    name: 'create', 
    component: CreatePost,
    meta: { requiresAuth: true }
  },
  { 
    path: '/post/:id', 
    name: 'postDetail', 
    component: PostDetail 
  },
  { 
    path: '/profile', 
    name: 'profile', 
    component: Profile,
    meta: { requiresAuth: true }
  },
  { 
    path: '/favorites', 
    name: 'favorites', 
    component: MyFavorites,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else {
    next()
  }
})

export default router
