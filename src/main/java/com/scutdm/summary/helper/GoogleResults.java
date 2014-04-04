package com.scutdm.summary.helper;

import java.util.List;

public class GoogleResults {
	private ResponseData responseData;
    public ResponseData getResponseData() { return responseData; }
    public void setResponseData(ResponseData responseData) { this.responseData = responseData; }
    @Override
	public String toString() { return "ResponseData[" + responseData + "]"; }
 
    static class ResponseData {
        private List<Result> results;
        public List<Result> getResults() { return results; }
        public void setResults(List<Result> results) { this.results = results; }
        @Override
		public String toString() { return "Results[" + results + "]"; }
    }
 
    static class Result {
        private String url;
        private String title;
        public String getUrl() { return url; }
        public String getTitle() { return title; }
        public void setUrl(String url) { this.url = url; }
        public void setTitle(String title) { this.title = title; }
        @Override
		public String toString() { return "Result[url:" + url +",title:" + title + "]"; }
    }
    
    /**
	for(int i = 0; i < totalSize;){
		// Google news api was deprecated
		// consider to use Google News RSS api or use Bing News Search api
		// http://news.google.com/news?q=[keywords]&output=rss			
		String address = "http://ajax.googleapis.com/ajax/services/search/news?v=1.0&start="+i+"&q=";
		String query = keyWords;
		String charset = "UTF-8";

		URL url = new URL(address + URLEncoder.encode(query, charset));
		Reader reader = new InputStreamReader(url.openStream(), charset);
		GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);

		int total = results.getResponseData().getResults().size();

		// Show title and URL of each results
		for(int j=0; j < total && i < totalSize; j++,i++){
			System.out.println("Title: " + results.getResponseData().getResults().get(j).getTitle());
			System.out.println("URL: " + results.getResponseData().getResults().get(j).getUrl() + "\n");
		}							
	}
	**/
}
