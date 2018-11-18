package com.pmaher.rest;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.pmaher.model.ShortUrl;
import com.pmaher.service.ShortUrlRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class ShortUrlController {
	
	@Autowired
	ShortUrlRepository shortUrlRepository;
	
	@PostMapping(value = "api/shortUrl", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<ShortUrl> createShortUrl( @RequestBody ObjectNode json) {	
		
		ShortUrl shortUrl = new ShortUrl(json.get("originalUrl").asText());
		shortUrl = shortUrlRepository.save(shortUrl);
		return new ResponseEntity<ShortUrl>(shortUrl, HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "api/shortUrl/{hashedKey}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<ShortUrl> createShortUrl(@PathVariable("hashedKey") String hashedKey) {
		ShortUrl shortUrl = this.shortUrlRepository.findOne(ShortUrl.getIdFromHashedKey(hashedKey));
		if(shortUrl != null) {
			return new ResponseEntity<ShortUrl>(shortUrl, HttpStatus.OK);			
		} else {
			return new ResponseEntity<ShortUrl>(HttpStatus.NO_CONTENT);
		}
	}
	
	@RequestMapping(value = "api/shortUrl/{hashedKey}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Void> deleteShortUrl(@PathVariable("hashedKey") String hashedKey) {
		
		this.shortUrlRepository.delete(ShortUrl.getIdFromHashedKey(hashedKey));
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
	
	@RequestMapping(value = "go/{hashedKey}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void redirectUserToOriginalUrl(@PathVariable("hashedKey") String hashedKey, HttpServletResponse response) {
		ShortUrl shortUrl = this.shortUrlRepository.findOne(ShortUrl.getIdFromHashedKey(hashedKey));
		if(shortUrl != null) {
			shortUrl.setTimesAccessed(shortUrl.getTimesAccessed()+1);
			shortUrlRepository.save(shortUrl);
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0");
			response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
			response.setHeader("Location", shortUrl.getOriginalUrl());
		} else {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
	}
}
