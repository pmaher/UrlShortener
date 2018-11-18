package com.pmaher.service;

import org.springframework.data.repository.CrudRepository;

import com.pmaher.model.ShortUrl;

public interface ShortUrlRepository extends CrudRepository<ShortUrl, Long> { }
