package com.example.tune_share_hub_backend.global.config.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(List.class)
public class ListStringTypeHandler extends BaseTypeHandler<List<String>> {
    private static final String DELIMITER = ","; // 태그 구분자

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        // List<String>을 콤마로 구분된 String으로 변환하여 DB에 저장
        String value = parameter != null ? String.join(DELIMITER, parameter) : null;
        ps.setString(i, value);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // DB에서 String을 가져와 List<String>으로 변환
        String value = rs.getString(columnName);
        return convertStringToList(value);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // DB에서 String을 가져와 List<String>으로 변환
        String value = rs.getString(columnIndex);
        return convertStringToList(value);
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // DB에서 String을 가져와 List<String>으로 변환
        String value = cs.getString(columnIndex);
        return convertStringToList(value);
    }

    private List<String> convertStringToList(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null; // 또는 Collections.emptyList();
        }
        return Arrays.stream(value.split(DELIMITER))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
