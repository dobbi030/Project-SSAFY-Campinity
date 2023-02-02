package com.ssafy.campinity.core.service;


import com.ssafy.campinity.core.dto.MyCollectionReqDTO;
import com.ssafy.campinity.core.entity.MyCollection.MyCollection;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

@Service
public interface MyCollectionService {

    MyCollection createMyCollection(MyCollectionReqDTO myCollectionReqDTO, int memberId);

    List<MyCollection> getMyCollections(int memberId);

    List<MyCollection> getLatestMyCollections(int memberId);

    MyCollection getMyCollection(String collectionUuid);

    MyCollection editMyCollection(MyCollectionReqDTO myCollectionReqDTO, String collectionUuid, int memberId) throws UnsupportedEncodingException, FileNotFoundException;

    void deleteMyCollection(String collectionUuid, int memberId) throws FileNotFoundException;

}
