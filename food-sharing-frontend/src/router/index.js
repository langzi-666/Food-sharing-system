import { createRouter, createWebHistory } from 'vue-router'

// 懒加载组件
const Home = () => import('@/views/Home.vue')
const Login = () => import('@/views/Login.vue')
const Register = () => import('@/views/Register.vue')
const CreatePost = () => import('@/views/CreatePost.vue')
const PostDetail = () => import('@/views/PostDetail.vue')
const Profile = () => import('@/views/Profile.vue')
const MyFavorites = () => import('@/views/MyFavorites.vue')
const MerchantRegister = () => import('@/views/MerchantRegister.vue')
const MerchantManage = () => import('@/views/MerchantManage.vue')
const AdminLayout = () => import('@/views/admin/AdminLayout.vue')
const Dashboard = () => import('@/views/admin/Dashboard.vue')
const UserManagement = () => import('@/views/admin/UserManagement.vue')
const PostReview = () => import('@/views/admin/PostReview.vue')
const SystemConfig = () => import('@/views/admin/SystemConfig.vue')
const SystemLogs = () => import('@/views/admin/SystemLogs.vue')

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
  },
  { 
    path: '/merchant/register', 
    name: 'merchantRegister', 
    component: MerchantRegister,
    meta: { requiresAuth: true }
  },
  { 
    path: '/merchant/manage', 
    name: 'merchantManage', 
    component: MerchantManage,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: 'dashboard', name: 'adminDashboard', component: Dashboard },
      { path: 'users', name: 'adminUsers', component: UserManagement },
      { path: 'posts', name: 'adminPosts', component: PostReview },
      { path: 'configs', name: 'adminConfigs', component: SystemConfig },
      { path: 'logs', name: 'adminLogs', component: SystemLogs },
      { path: '', redirect: '/admin/dashboard' }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
    return
  }
  
  // 检查管理员权限
  if (to.meta.requiresAdmin) {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      if (userInfo.role !== 'ROLE_ADMIN') {
        // 如果不是管理员，尝试获取最新用户信息
        const { getUserInfo } = await import('@/api/users')
        const response = await getUserInfo()
        // axios拦截器已经返回了response.data，所以直接使用response
        const role = response?.role
        localStorage.setItem('userInfo', JSON.stringify(response))
        
        if (role !== 'ROLE_ADMIN') {
          next('/')
          return
        }
      }
    } catch (error) {
      console.error('检查管理员权限失败:', error)
      next('/')
      return
    }
  }
  
  next()
})

export default router
