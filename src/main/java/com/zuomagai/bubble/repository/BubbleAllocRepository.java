package com.zuomagai.bubble.repository;

import com.zuomagai.bubble.model.BubbleAlloc;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BubbleAllocRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BubbleAllocRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BubbleAlloc findByBizTag(String bizTag) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT biz_tag, max_id, step, version FROM bubble_alloc WHERE biz_tag = :bizTag",
                    new MapSqlParameterSource("bizTag", bizTag),
                    new BeanPropertyRowMapper<>(BubbleAlloc.class));
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public BubbleAlloc updateMaxId(String bizTag, long currentMaxId, int step, int expectedVersion) {
        long newMaxId = currentMaxId + step;
        int updated = jdbcTemplate.update(
                "UPDATE bubble_alloc SET max_id = :maxId, step = :step, version = :newVersion WHERE biz_tag = :bizTag AND version = :version",
                new MapSqlParameterSource()
                        .addValue("maxId", newMaxId)
                        .addValue("step", step)
                        .addValue("newVersion", expectedVersion + 1)
                        .addValue("bizTag", bizTag)
                        .addValue("version", expectedVersion));
        if (updated == 0) {
            return null;
        }
        BubbleAlloc alloc = new BubbleAlloc();
        alloc.setBizTag(bizTag);
        alloc.setMaxId(newMaxId);
        alloc.setStep(step);
        alloc.setVersion(expectedVersion + 1);
        return alloc;
    }
}
