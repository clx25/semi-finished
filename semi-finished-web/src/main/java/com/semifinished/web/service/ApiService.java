package com.semifinished.web.service;

import com.semifinished.core.annontation.Api;
import com.semifinished.core.service.QueryAbstractService;
import com.semifinished.core.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Api(path = "/semiApi", method = "post")
@Service
@RequiredArgsConstructor
public class ApiService extends QueryAbstractService implements QueryService {

}
