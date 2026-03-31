create table if not exists sys_user (
  id bigint primary key auto_increment,
  username varchar(50) not null,
  password_hash varchar(100) not null,
  role varchar(20) not null,
  status varchar(20) not null default 'ACTIVE',
  created_at datetime not null,
  unique key uk_user_username (username)
) engine=InnoDB default charset=utf8mb4;
