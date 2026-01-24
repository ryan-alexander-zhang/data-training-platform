package com.example.training.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.training.infra.entity.DatasetFileEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DatasetFileMapper extends BaseMapper<DatasetFileEntity> {
}
