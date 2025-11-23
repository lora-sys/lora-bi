import type {RequestConfig, RunTimeLayoutConfig} from '@umijs/max';

import {history} from '@umijs/max';

import React from 'react';

import {AvatarDropdown, AvatarName, Footer, Question} from '@/components';

import {errorConfig} from './requestErrorConfig';

import '@ant-design/v5-patch-for-react-19';

import {getLoginUserUsingGet, getUserScoreUsingGet} from "@/services/lora-bi/userController";


const isDev = process.env.NODE_ENV === 'development';
const isDevOrTest = isDev || process.env.CI;
const loginPath = '/user/login';

/**
 *
 * */
export async function getInitialState(): Promise<{
  currentUser?: API.LoginUserVO;
  userScore?: number;
}> {
  const fetchUserInfo = async () => {
    try {
      const msg = await getLoginUserUsingGet();
      return msg.data;
    } catch (_error) {
      history.push(loginPath);
    }
    return undefined;
  };

  const fetchUserScore = async () => {
    try {
      const res = await getUserScoreUsingGet();
      if (res.data !== undefined) {
        return res.data;
      }
    } catch (_error) {
      console.error("获取用户积分失败:", _error);
    }
    return 0;
  };

  // 如果不是登录页面，执行

  const {location} = history;
  if (![loginPath, '/user/register', '/user/register-result'].includes(location.pathname)) {
    const currentUser = await fetchUserInfo();
    if (currentUser) {
      const userScore = await fetchUserScore();
      return {
        currentUser,
        userScore,
      };
    } else {
      return {};
    }
  }
  return {};
}


// ProLayout 支持的api https://procomponents.ant.design/components/layout
export const layout: RunTimeLayoutConfig = ({initialState, setInitialState}) => {
  return {
    actionsRender: () => [<Question key="doc"/>],
    avatarProps: {
      src: initialState?.currentUser?.userAvatar,
      title: <AvatarName/>,
      render: (_, avatarChildren) => <AvatarDropdown>{avatarChildren}</AvatarDropdown>,
    },
    waterMarkProps: {
      content: initialState?.currentUser?.userName,
    },
    footerRender: () => <Footer/>,
    onPageChange: () => {
      const {location} = history;
      // 如果没有登录，重定向到 login
      if (!initialState?.currentUser && location.pathname !== loginPath) {
        history.push(loginPath);
      }
    },
    bgLayoutImgList: [
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/D2LWSqNny4sAAAAAAAAAAAAAFl94AQBr',
        left: 85,
        bottom: 100,
        height: '303px',
      },
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/C2TWRpJpiC0AAAAAAAAAAAAAFl94AQBr',
        bottom: -68,
        right: -45,
        height: '303px',
      },
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/F6vSTbj8KpYAAAAAAAAAAAAAFl94AQBr',
        bottom: 0,
        left: 0,
        width: '331px',
      },
    ],
    menuHeaderRender: undefined,
    // 自定义 403 页面
    unAccessible: <div>unAccessible</div>
  };
};

/**
 * @name request 配置，可以配置错误处理
 * 它基于 axios 和 ahooks 的 useRequest 提供了一套统一的网络请求和错误处理方案。
 * @doc https://umijs.org/docs/max/request#配置
 */
export const request: RequestConfig = {
  baseURL: 'http://localhost:8101',
  withCredentials: true,
  ...errorConfig,
};
