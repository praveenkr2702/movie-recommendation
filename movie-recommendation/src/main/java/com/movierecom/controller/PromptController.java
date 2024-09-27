package com.movierecom.controller;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.movierecom.service.MovieService;

import okhttp3.*;
import org.json.*;

@Controller
public class PromptController {
	
	@Autowired
	MovieService movieService;
	
	private String storedPrompt;
	
	@GetMapping("/")
	public String main()
	{
		return "index";
	}
	@PostMapping("/movieList")
	public String getPrompt(@RequestParam String prompt,Model model) throws IOException
	{
		
		this.storedPrompt=prompt;
		model.addAttribute("prompt",prompt);
		
		
		//skipping the AI functionality as of now
		java.util.List<ArrayList<String>> list = movieService.getKeyWordId(storedPrompt);
		
		ArrayList<String> movieNames=list.get(0);
		ArrayList<String> posterPaths = list.get(1);
		
		model.addAttribute("movieNames", movieNames);
		model.addAttribute("posterPaths", posterPaths);

		
			
		return "results";
	}

}
