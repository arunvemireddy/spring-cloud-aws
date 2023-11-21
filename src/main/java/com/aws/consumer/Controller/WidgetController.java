package com.aws.consumer.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.aws.consumer.DTO.Widget;

//@RestController
//@CrossOrigin(origins = "*")
public class WidgetController {

//    @GetMapping(path = "/welcome")
//    public ResponseEntity<String> welcome(){
//        return new ResponseEntity<String>("hello, welcome", HttpStatus.OK);
//    }
//
//    @PostMapping(path="/widgetRequest")
//    public ResponseEntity<String> process(@RequestBody Widget widget){
//        return new ResponseEntity<String>(widget.toString(), HttpStatus.OK);
//    }
}
