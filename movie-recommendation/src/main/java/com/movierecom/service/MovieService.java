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
		String finalPrompt="x: "+storedPrompt+"Return , separated categories (mltiple cat pssible) x will fall into, no extra text: Action,Adventure,Animation,Comedy,Crime,Documentary,Drama,Family,Fantasy,History,Horror,Mystery,Romance,Science Fiction,Thriller";
		
		String identifiedGenred;
		
		identifiedGenred = chatClient.prompt().user(finalPrompt).call().content();
		
		String[] genreArray = identifiedGenred.split(",\\s*");
        System.out.println("output >> "+ identifiedGenred);
        
        return genreArray;

	}
	
	

	public List<ArrayList<String>> getGenreId(String storedPrompt) throws IOException 
	{
		
		
		String[] genreArray = promptAi(storedPrompt);
		String jsonString ="";
		JSONObject jsonObject = null;
		
		HashMap<Integer,String> genreList = new HashMap<>();
		
		String genreIds = "";
		
		OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder()
		  .url("https://api.themoviedb.org/3/genre/movie/list?language=en")
		  .get()
		  .addHeader("accept", "application/json")
		  .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyMzMxZjQ1YzkyNWE0MzRkNmJmOTQzNjI1ODQ1ZjE0YyIsIm5iZiI6MTcyNzE4OTI3MS40MjMxNjEsInN1YiI6IjY2ZjI1OTgyZmMwMDk4MzkxNDhkNjQxYyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.2eUDtoCGASghQkv5W0pLZ0uR1DebsXqK1kQDqS_O4fQ")
		  .build();

		Response response = client.newCall(request).execute();
		
	
		
		if(response.isSuccessful() && response.body()!=null)
		{
			
			jsonString=response.body().string();
			
			jsonObject = new JSONObject(jsonString);
			
			System.out.println(jsonString); 
			
			JSONArray jsonArray = jsonObject.getJSONArray("genres");
			
			
			
			boolean genreFound=false;
			
			
			for(int j=0;j<genreArray.length;j++)
			{
			
				for(int i=0;i<jsonArray.length();i++)
				{
					JSONObject tempJsonObj = jsonArray.getJSONObject(i);
					
					genreList.put(tempJsonObj.getInt("id"), tempJsonObj.getString("name"));
				
					if(tempJsonObj.getString("name").toLowerCase().equals(genreArray[j].toLowerCase()) )
					{
						genreFound=true;
						System.out.println("genre name is "+tempJsonObj.getString("name"));
						System.out.println("genre id is "+tempJsonObj.getInt("id"));
						
						
						
						if(j==genreArray.length-1)
						{
							genreIds=genreIds+String.valueOf(tempJsonObj.getInt("id"));
						}
						else
						{
							genreIds=genreIds+String.valueOf(tempJsonObj.getInt("id"))+"%7C";
						}
						
						
					
					}
				
				}
			}
			
			if(!genreFound)
			{
				System.out.println("genre not found");
			}
		}
		
		System.out.println("genreIds >>> "+genreIds);
		
		List<ArrayList<String>> x = movieRecommendation(genreIds, genreList);
		
		 return x;
		
		
		
	}

	public List<ArrayList<String>> movieRecommendation(String genreIds, HashMap<Integer,String> genreList ) throws IOException {
		
		OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder()
		  .url("https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc&with_genres="+genreIds)
		  .get()
		  .addHeader("accept", "application/json")
		  .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyMzMxZjQ1YzkyNWE0MzRkNmJmOTQzNjI1ODQ1ZjE0YyIsIm5iZiI6MTcyNzE4OTI3MS40MjMxNjEsInN1YiI6IjY2ZjI1OTgyZmMwMDk4MzkxNDhkNjQxYyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.2eUDtoCGASghQkv5W0pLZ0uR1DebsXqK1kQDqS_O4fQ")
		  .build();

		
		Response response = client.newCall(request).execute();
		
		String movieJsonString = response.body().string();
		
		System.out.println("movie json >>> "+movieJsonString);
		
		
		
		JSONObject movieJsonObject = new JSONObject(movieJsonString);
		
		int totalPages=movieJsonObject.getInt("total_pages");
		
		JSONArray movieJsonArray = movieJsonObject.getJSONArray("results");
		
		ArrayList<String> movieNames = new ArrayList<>();
		ArrayList<String> posterPaths = new ArrayList<>();
		
		
		for(int i=0;i<movieJsonArray.length();i++)
		{
			JSONObject tempJsonObj = movieJsonArray.getJSONObject(i);
			movieNames.add(tempJsonObj.getString("original_title"));
			posterPaths.add("https://image.tmdb.org/t/p/w185"+tempJsonObj.getString("poster_path"));
		}
		
		return Arrays.asList(movieNames,posterPaths);
		
	}

}
