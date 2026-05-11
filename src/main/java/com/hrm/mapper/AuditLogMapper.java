package com.hrm.mapper;

import com.hrm.domain.AuditLog;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuditLogMapper {

	@Select("select id, module, action, target_type, target_id, actor, detail, before_value, after_value, ip_address, created_at "
			+ "from audit_log order by id desc limit 200")
	List<AuditLog> findRecent();

	@Insert("insert into audit_log(module, action, target_type, target_id, actor, detail, before_value, after_value, ip_address, created_at) "
			+ "values(#{module}, #{action}, #{targetType}, #{targetId}, #{actor}, #{detail}, #{beforeValue}, #{afterValue}, #{ipAddress}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(AuditLog log);
}
