package com.example.training.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.training.infra.entity.DatasetEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DatasetMapper extends BaseMapper<DatasetEntity> {
}
