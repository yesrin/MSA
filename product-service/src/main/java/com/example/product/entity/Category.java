package com.example.product.entity;

/**
 * 상품 카테고리
 */
public enum Category {
    ELECTRONICS("전자제품"),
    FASHION("패션"),
    FOOD("식품"),
    BEAUTY("뷰티"),
    SPORTS("스포츠"),
    BOOK("도서"),
    HOME("홈/리빙"),
    TOY("장난감"),
    ETC("기타");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
