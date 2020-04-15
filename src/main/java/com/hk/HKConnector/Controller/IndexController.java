package com.hk.HKConnector.Controller;

import com.hk.HKConnector.Service.*;
import com.hk.HKConnector.model.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;

import java.util.*;

@Controller
public class IndexController {

    @Autowired
    private BulkProcessConfigService bulkProcessConfigService;

    @RequestMapping("/")
    public String index() {
        return "dashboard.html";
    }
}
