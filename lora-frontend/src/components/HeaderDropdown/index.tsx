import { Dropdown } from 'antd';
import type { DropDownProps } from 'antd/es/dropdown';
import { createStyles } from 'antd-style';

import classNames from 'classnames';

export type HeaderDropdownProps = {
  overlayClassName?: string;
  placement?: 'bottomLeft' | 'bottomRight' | 'topLeft' | 'topCenter' | 'topRight' | 'bottomCenter';
} & Omit<DropDownProps, 'overlay'>;

const useStyles = createStyles(({ token }) => {
  return {
    [`@media screen and (max-width: ${token.screenXS})`]: {
      width: '100%',
    },
  };
});

const HeaderDropdown: React.FC<HeaderDropdownProps> = ({ overlayClassName: cls, ...restProps }) => {
  const { styles } = useStyles();
  return <Dropdown overlayClassName={classNames(styles, cls)} {...restProps} />;
};

export default HeaderDropdown;
