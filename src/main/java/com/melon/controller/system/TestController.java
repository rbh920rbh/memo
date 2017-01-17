package com.melon.controller.system;

import com.melon.data.User;
import com.melon.utils.TalentResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Baihua on 17-1-17.
 */
@RestController
@RequestMapping("/test")
public class TestController {

    private static final Logger logger= LoggerFactory.getLogger(TestController.class);

    @RequestMapping(value = "/{id}" ,method = RequestMethod.GET)
    public void test(HttpServletRequest request,
                     HttpServletResponse response,
                     @PathVariable("id") String id){
//        model.addAttribute("counter",++counter);
        logger.debug("log~~" + id);
        User user = new User();
        user.setId(id);
        user.setName("rbh");
//        return user;//返回index.jsp
        TalentResource.SUCCESS(request, response).append("test", "hello world").respond();
    }

//    @RequestMapping(value = "/{name}" ,method = RequestMethod.GET)
//    public String welcome(@PathVariable String name , ModelMap model){
//        model.addAttribute("message","Welcome "+name);
//        model.addAttribute("counter",++counter);
//        logger.debug("[Welcome counter :{}",counter);
//        return VIEW_INDEX;//返回index.jsp
//    }

}
