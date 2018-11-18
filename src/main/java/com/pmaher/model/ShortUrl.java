package com.pmaher.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table
@Entity
public class ShortUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String originalUrl;
	@Transient
	private String hashedKey;
	private int timesAccessed;
	
	private static final String CHARACTER_MAP = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int BASE = CHARACTER_MAP.length();
	
	public ShortUrl() {
		super();
	}
	
	public ShortUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}

	public int getTimesAccessed() {
		return timesAccessed;
	}

	public void setTimesAccessed(int timesAccessed) {
		this.timesAccessed = timesAccessed;
	}

	public String getHashedKey() {
		return ShortUrl.getHashedKeyFromId(this.id);
	}
	
	public void setHashedKey(String key) {
		this.hashedKey = key;
	}
	
	public static Long getIdFromHashedKey(String hashedKey) {
		int hashedKeyLen = hashedKey.length();
		long idToReturn = 0L;
		
		for(int power = hashedKeyLen-1; power >= 0; power--) {
			char theCharacter = hashedKey.charAt((hashedKeyLen-1) - power);
			int multiplier = CHARACTER_MAP.indexOf(theCharacter);
			idToReturn += (multiplier * Math.pow(BASE, power));
		}
		return idToReturn;
	}
	
	public static String getHashedKeyFromId(Long id) {
		StringBuilder builder = new StringBuilder();
		int num = id.intValue();
		while(num > 0) {
			int remainder = num % BASE;
			num = num / BASE;
			builder.append(CHARACTER_MAP.charAt(remainder));
		}
		return builder.reverse().toString();
	}

}
