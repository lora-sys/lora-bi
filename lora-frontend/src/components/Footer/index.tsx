import { GithubOutlined } from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
import React from 'react';

const Footer: React.FC = () => {
  return (
    <DefaultFooter
      style={{
        background: 'none',
      }}
      copyright="Powered by lora"
      links={[
        {
          key: 'lora 智能BI ',
          title: 'lora 智能BI ',
          href: 'https://lora-bi.com',
          blankTarget: true,
        },
        {
          key: 'github',
          title: <GithubOutlined />,
          href: 'https://github.com/lora-sys/lora-bi',
          blankTarget: true,
        },
        {
          key: 'lora 智能BI',
          title: 'lora 智能BI',
          href: 'https://ant.design',
          blankTarget: true,
        },
      ]}
    />
  );
};

export default Footer;
