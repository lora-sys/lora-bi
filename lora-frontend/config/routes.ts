export default [{
    path: '/user',
    layout: false,
    routes: [{name: '登录', path: '/user/login', component: './user/login'},
      {name: '注册', path: '/user/register', component: './user/register'}
    ],
  },
  {path: '/',name: '首页', redirect: '/'},
  {path: '/add_chart',name: '智能分析', icon: 'HeatMap', component: './AddChart'},
  {path: '/add_chart_async',name: '智能分析 (异步)', icon: 'HeatMap', component: './AddChartAsync'},
  {path: '/my-chart', name: '我的图表', icon: 'PieChart', component: './MyChart'},
  {path: '/chart-manage', name: '图表管理', icon: 'BarChart', component: './ChartManage'},
  {path: '/edit-chart/:id', name: '编辑图表', icon: 'Edit', component: './EditChart', hideInMenu: true},
  {
    path: '/admin',
    name: '管理页',
    icon: 'crown',
    access: 'canAdmin',
    routes: [
      {path: '/admin', redirect: '/admin/sub-page'},
      {path: '/admin/sub-page', name: '二级管理页', component: './Admin'},
    ],
  },
  {path: '/', redirect: '/welcome'},
  {component: '404', layout: false, path: './*'},
];
