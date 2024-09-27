package com.movierecom.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class MovieService {
	
	private ChatClient chatClient;
	
	MovieService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
	
	public String[] promptAi(String storedPrompt)
	{
		String finalPrompt="convert into three comma separated easy keywords (avlbl in tmdb movie website) from: "+storedPrompt;
		//String finalPrompt="x: "+storedPrompt+"Return , separated categories (mltiple cat pssible) x will fall into, no extra text: Action,Adventure,Animation,Comedy,Crime,Documentary,Drama,Family,Fantasy,History,Horror,Mystery,Romance,Science Fiction,Thriller";
		
		String identifiedKeywords;
		
		identifiedKeywords = chatClient.prompt().user(finalPrompt).call().content();
		
		String[] keyWordArray = identifiedKeywords.split(",\\s*");
        System.out.println("identifiedKeywords >> "+ identifiedKeywords);
        
        return keyWordArray;

	}
	
	

	public List<ArrayList<String>> getKeyWordId(String storedPrompt) throws IOException 
	{
		
		
		String[] keyWordArray = promptAi(storedPrompt);
		
		String keyWordIds= "";
		
		
		
		String jsonString ="";
		JSONObject jsonObject = null;
		
		int c=0;
		
		
		for(int i=0;i<keyWordArray.length;i++)
		{
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder()
			  .url("https://api.themoviedb.org/3/search/keyword?query="+keyWordArray[i]+"&page=1")
			  .get()
			  .addHeader("accept", "application/json")
			  .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyMzMxZjQ1YzkyNWE0MzRkNmJmOTQzNjI1ODQ1ZjE0YyIsIm5iZiI6MTcyNzE4OTI3MS40MjMxNjEsInN1YiI6IjY2ZjI1OTgyZmMwMDk4MzkxNDhkNjQxYyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.2eUDtoCGASghQkv5W0pLZ0uR1DebsXqK1kQDqS_O4fQ")
			  .build();

			Response response = client.newCall(request).execute();
			if(response.isSuccessful() && response.body()!=null)
			{
			
				jsonString =response.body().string();
			
				jsonObject = new JSONObject(jsonString);
			
				int total_results = jsonObject.getInt("total_results");
			
				if(total_results==0)
				{
					c++;
					continue;
				}
			
				JSONArray jsonArray=jsonObject.getJSONArray("results");
			
				int limit=0;
				if(jsonArray.length()>=2)
				{
					limit=2;
				}
				else if(jsonArray.length()==1)
				{
					limit=1;
				}
				
	
			
				for(int j=0;j<limit;j++)
				{
					JSONObject tempJsonObj = jsonArray.getJSONObject(j);
	
					keyWordIds=keyWordIds+String.valueOf(tempJsonObj.getInt("id"))+"%7C";
				}
			}
			
		}
		
		if(c==3)
		{
			
		}
		//keyWordIds = keyWordIds.substring(0, keyWordIds.lastIndexOf("%7c")) + keyWordIds.substring(keyWordIds.lastIndexOf("%7c") + 3);

		System.out.println("keyWordIds>>> "+keyWordIds);
		List<ArrayList<String>> x = movieRecommendation(keyWordIds);
		
		return x;
		
		
		
	}

	public List<ArrayList<String>> movieRecommendation(String keyWordIds) throws IOException {
		
		OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder()
		  .url("https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc&with_keywords="+keyWordIds)
		  .get()
		  .addHeader("accept", "application/json")
		  .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyMzMxZjQ1YzkyNWE0MzRkNmJmOTQzNjI1ODQ1ZjE0YyIsIm5iZiI6MTcyNzE4OTI3MS40MjMxNjEsInN1YiI6IjY2ZjI1OTgyZmMwMDk4MzkxNDhkNjQxYyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.2eUDtoCGASghQkv5W0pLZ0uR1DebsXqK1kQDqS_O4fQ")
		  .build();

		Response response = client.newCall(request).execute();
		
		String movieJsonString = response.body().string();
		
		System.out.println("movie json >>> "+movieJsonString);
		
		
		
		JSONObject movieJsonObject = new JSONObject(movieJsonString);
		
		JSONArray movieJsonArray = movieJsonObject.getJSONArray("results");
		
		ArrayList<String> movieNames = new ArrayList<>();
		ArrayList<String> posterPaths = new ArrayList<>();
		
		
		for(int i=0;i<movieJsonArray.length();i++)
		{
			JSONObject tempJsonObj = movieJsonArray.getJSONObject(i);
			movieNames.add(tempJsonObj.getString("original_title"));
			posterPaths.add("https://image.tmdb.org/t/p/w185"+tempJsonObj.optString("poster_path"));
		}
		
		return Arrays.asList(movieNames,posterPaths);
		
	}

}
