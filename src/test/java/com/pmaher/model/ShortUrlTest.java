package com.pmaher.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ShortUrlTest {
	
	@Test
	public void testGetIdFromHashedKey() {
		verifyHashedKeyMapsToId("b9c", 7628L);
		verifyHashedKeyMapsToId("A12ac", 397023698L);
		verifyHashedKeyMapsToId("fI", 344L);
	}
	
	@Test
	public void hashedKeyIsSetAutomatically() {
		//Given a ShortUrl with an ID
		ShortUrl shortUrl = new ShortUrl("http://test.com");
		shortUrl.setId(344L);
		//Then the hashedKey will be set automatically
		assertThat(shortUrl.getHashedKey()).isEqualTo("fI");
	}
	
	private void verifyHashedKeyMapsToId(String expectedHashedKey, Long expectedId) {
		
		//When looking up the ID from the hashed key
		Long actualId = ShortUrl.getIdFromHashedKey(expectedHashedKey);
		
		//Then the ID should match the expected
		assertThat(actualId).isEqualTo(expectedId);
		
		//When looking up the hashedKey from the id
		String actualHashedKey = ShortUrl.getHashedKeyFromId(expectedId);
		
		//Then the hashedKey should match the expectedHashedKey
		assertThat(actualHashedKey).isEqualTo(expectedHashedKey);
		
		
	}

}
