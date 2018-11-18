package com.pmaher.rest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.pmaher.app.Application;
import com.pmaher.model.ShortUrl;
import com.pmaher.service.ShortUrlRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=Application.class)
@WebAppConfiguration
@AutoConfigureMockMvc
public class ShortUrlControllerTest {
	
	@Autowired
	ShortUrlController shortUrlController;
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	ShortUrlRepository shortUrlRepository;
	
	@Test
	public void testCreateShortUrl() throws Exception {
    	ShortUrl shortUrl = new ShortUrl("http://testing.com/");
    	shortUrl.setId(999L);
    	shortUrl.setTimesAccessed(0);
    	
    	given(this.shortUrlRepository.save(any(ShortUrl.class))).willReturn(shortUrl);
    	ObjectMapper mapper = new ObjectMapper();
    	String shortUrlAsJSON = mapper.writeValueAsString(shortUrl);
    	System.out.println(shortUrlAsJSON);
		//WHEN making an http request to create a shortUrl
    	MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/api/shortUrl")
    			.contentType(MediaType.APPLICATION_JSON_VALUE)
    			.content(shortUrlAsJSON)
    			.accept(MediaType.APPLICATION_JSON_VALUE);
    	
    	ResultActions resultActions = mockMvc.perform(builder);
    	
    	//THEN expect the newly created shortUrl to be returned
    	resultActions
    		.andExpect(status().isCreated())
	    	.andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.id").value(999))
            .andExpect(jsonPath("$.originalUrl").value(shortUrl.getOriginalUrl()))
            .andExpect(jsonPath("$.hashedKey").value("qh"))
            .andExpect(jsonPath("$.timesAccessed").value(shortUrl.getTimesAccessed()));

	}
	
	@Test
	public void testGetShortUrlByHashedKey() throws Exception {
		ShortUrl shortUrl = new ShortUrl("testing.com");
		shortUrl.setId(7628L);
		shortUrl.setHashedKey("b9c");
		shortUrl.setTimesAccessed(3);
		given(this.shortUrlRepository.findOne(7628L)).willReturn(shortUrl);
		
		//WHEN making an http request to create a shortUrl
		this.mockMvc.perform(get("/api/shortUrl/b9c")
							.accept(MediaType.APPLICATION_JSON_VALUE)
							.contentType(MediaType.APPLICATION_JSON_VALUE))
							//THEN expect the newly created shortUrl to be returned
							.andExpect(status().isOk())
							.andExpect(content().contentType("application/json;charset=UTF-8"))
				            .andExpect(jsonPath("$.id").value(7628))
				            .andExpect(jsonPath("$.originalUrl").value("testing.com"))
				            .andExpect(jsonPath("$.hashedKey").value("b9c"))
				            .andExpect(jsonPath("$.timesAccessed").value("3"));
	}
	
	@Test
	public void testGetShortUrlByHashedKeyNotFound() throws Exception {
		ShortUrl shortUrl = new ShortUrl("testing.com");
		shortUrl.setId(7628L);
		shortUrl.setHashedKey("b9c");
		shortUrl.setTimesAccessed(3);
		given(this.shortUrlRepository.findOne(7628L)).willReturn(null);
		
		//WHEN making an http request to create a shortUrl
		this.mockMvc.perform(get("/api/shortUrl/b9c")
							.accept(MediaType.APPLICATION_JSON_VALUE)
							.contentType(MediaType.APPLICATION_JSON_VALUE))
							//THEN expect the newly created shortUrl to be returned
							.andExpect(status().isNoContent());
	}
	
	@Test
	public void testDeleteShortUrl() throws Exception {
		
		//WHEN making an http request to create a shortUrl
		this.mockMvc.perform(delete("/api/shortUrl/b9c"))
							//THEN expect the shortUrl to be deleted
							.andExpect(status().isNoContent());	
		
		verify(shortUrlRepository, times(1)).delete(ShortUrl.getIdFromHashedKey("b9c"));
	}

	@Test
	public void testRedirectUserToOriginalUrlAndIncrementsTimesAccessed() throws Exception {
		ShortUrl expected = new ShortUrl("testing.com");
		expected.setId(7628L);
		expected.setTimesAccessed(4);
		
		ShortUrl shortUrl = new ShortUrl("testing.com");
		shortUrl.setId(7628L);
		shortUrl.setHashedKey("b9c");
		shortUrl.setTimesAccessed(3);
		given(this.shortUrlRepository.findOne(7628L)).willReturn(shortUrl);
		
		//WHEN making an http request to navigate through a short url
		this.mockMvc.perform(get("/go/b9c"))
							//THEN the user should be redirected successfully
							.andExpect(status().isTemporaryRedirect())
							//AND the cache header should be cleared so that we can still increment the times accessed
							.andExpect(header().string("Cache-Control", equalTo("no-cache, no-store, must-revalidate")))
							.andExpect(header().string("Pragma", equalTo("no-cache")))
							.andExpect(header().string("Expires", equalTo("0")))
							.andExpect(header().string("Location", equalTo(shortUrl.getOriginalUrl())));
		
		verify(shortUrlRepository, times(1)).save(argThat(new ShortUrlMatcher(expected)));
	}
	

	@Test
	public void testNotFoundWhenRedirectingUserToOriginalUrl() throws Exception {
		
		given(this.shortUrlRepository.findOne(7628L)).willReturn(null);
		
		//WHEN making an http request to navigate through a short url
		this.mockMvc.perform(get("/go/b9c"))
							//THEN the result should have status no content
							.andExpect(status().isNoContent());
	}
	
	public class ShortUrlMatcher extends ArgumentMatcher<ShortUrl> {
		 
	    private ShortUrl expectedShortUrl;
	    
	    public ShortUrlMatcher(ShortUrl shortUrl) {
	    	this.expectedShortUrl = shortUrl;
	    }

		@Override
		public boolean matches(Object actual) {
			ShortUrl actualShortUrl = (ShortUrl) actual;
			return expectedShortUrl.getOriginalUrl().equals(actualShortUrl.getOriginalUrl())
					&& expectedShortUrl.getId().equals(actualShortUrl.getId())
					&& expectedShortUrl.getOriginalUrl().equals(actualShortUrl.getOriginalUrl());
		}
	}

}
