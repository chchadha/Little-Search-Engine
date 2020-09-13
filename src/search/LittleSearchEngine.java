package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 *
 * @author Sesh Venugopal
 *
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;

	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;

	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 *
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {

	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;

	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;

	private Scanner sc;

	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}

	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 *
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile)
			throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}

		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}

	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 *
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile)
			throws FileNotFoundException {

		/* keyWord is the first word in the file the programming is scanning.
		 * The keyWord we will be analyzing in the new hash table may have punctuation
		 * or may not even be considered a keyword. Therefore, we need to run the getKeyWord
		 * method on the String object (keyWord), which is the current word in the document.
		 * The getKeyWord will return a word or null and concatenate any punctuation at the end of the word
		 */

		sc = new Scanner(new File(docFile));

		HashMap<String, Occurrence> keyWords = new HashMap<String, Occurrence>();

		while(sc.hasNext() == true){
			String key = getKeyWord(sc.next());

			if(key!=null){

				key = key.toLowerCase();
				if(keyWords.containsKey(key)){
					Occurrence oldCount = keyWords.get(key);
					oldCount.frequency++;
				}

				else if(!key.equals(noiseWords)){  //Checks if the next word is not a noise word
					keyWords.put(key,new Occurrence(docFile,1)); // Then I add to my new hashtable
				}
			}
		}

		return keyWords;
	}

	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table.
	 * This is done by calling the insertLastOccurrence method.
	 *
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		Set<String> occur = kws.keySet(); //used the key set method and set modifer (found on hashmap api) to return a set view of the keys in the entire map
		String key[] = occur.toArray(new String[occur.size()]); //created an array which would hold the keys and used the .toArray method (found on java api) to place all the keys into one array

		/* I created a for loop which would check every key in the array key and check if it was already in my main hash(keywordsIndex).
		 * If the main hash consists of the key then I would add the words occurrence to the arraylist and sort the array list.
		 */

		for(int i =0; i<key.length; i++){
			String data = key[i];
			if(keywordsIndex.containsKey(key[i])){ // If my main hash already has the keyword then I need to add the new occurrence to the arraylist for that keyword
				ArrayList<Occurrence> occ = keywordsIndex.get(data);
				occ.add(kws.get(data));
				insertLastOccurrence(occ);
				keywordsIndex.put(data, occ);

			}else{ // My hashmap doesnt have the keyword , then the program will create a new key / value and place it into the hashmap
				ArrayList<Occurrence> occurrences = new ArrayList<Occurrence>();
				occurrences.add(kws.get(data));
				keywordsIndex.put(data, occurrences);
			}
		}
	}

	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 *
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 *
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		// COMPLETE THIS METHOD
		// THE FOLLOWING LINE HAS BEEN ADDED TO MAKE THE METHOD COMPILE

		/*
		 *  In this method I need to check if the word has any sort of punctuation in between such as
		 *  a "-" or "," and return null since it will not be a keyword. Otherwise, if the punctuation
		 *  is at the end of the word then I will keep it as a key word and use the substring method to
		 *  cut off any punctuation towards the end.
		 */

		/*
		 * In this for loop I began searching for the input word at position one. Since there can be a punctuation
		 * within the word I created my loop where my index began from the beginning and stops at the second to last
		 * letter in the word. If there is a punctuation mark in the word and there is a letter proceeding the punctuation
		 * then we return null since we know that the punctuation point is in the middle of the word
		 *
		 */
		if(noiseWords.containsKey(word) == true){
			return null;
		}


		/*
		 * First I want to check if there is any punction at the end of the word. If there is I would like to
		 * remove the punctutation and reduce word.length() until my word doesn't have any trailing spaces. Then
		 * I search from the front to see if I have any punctuation.
		 */

		for(int i = word.length()-1; 0<=i; i--){
			if(Character.isLetter(word.charAt(i)) == false){ //Trailing spaces have been deleted from the word
				word = word.substring(0, word.length()-1);
			}
		}

		for(int i =0; i < word.length(); i++){  //word and word length have been updated. I know there are not any trailing punctuation so I will search from the front for any punctuation and if there is any  I will return null since it is not a keyword
			if(Character.isLetter(word.charAt(i)) == false){ //if there is a punctuation anywhere in the updated word
				return null;
			}
		}

		word = word.toLowerCase(); // convert all the letters to lowercase and return word.
		if(noiseWords.containsKey(word) == true || word.length() ==0){
			return null;
		}
		return word;
	}

	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search,
	 * then inserting at that spot.
	 *
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {

		if(occs.size() <= 1){
			return null;
		}

		int n = occs.size();  // I know what my size is for the array list
		Occurrence insertOccurrence = occs.remove(n-1); // DATA I know what my occurrence is which in the last position of my array lise
		int freq = insertOccurrence.frequency; //Now I have my frequency of the last occurrence.

		ArrayList<Integer> indexVisited = new ArrayList<Integer>();

		int mid = occs.size() / 2;   //index
		int left = 0;
		int right = occs.size()-1;


		while(left <= right){
			mid = (right + left) / 2;
			indexVisited.add(mid);

			if(occs.get(mid).frequency > freq){
				left = mid+1;
			}
			else if (occs.get(mid).frequency < freq){
				right = mid-1;
			}
			else{
				break;
			}

		}
		if(occs.get(mid).frequency > freq){ // If my mids data was greater than frequency I add after the mid
			occs.add(mid+1, insertOccurrence);
		}
		occs.add(mid, insertOccurrence); //Otherwise it was less than you add before the mid

		return indexVisited;

	}

	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result.
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 *
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {

		if(!keywordsIndex.containsKey(kw1) && !keywordsIndex.containsKey(kw2)){ //If the merged (main) hashmap doesnt have either word return null. This is the base case
			return null;
		}
		ArrayList<String> documents = new ArrayList<String>();
		ArrayList<Occurrence> freqKW1 = keywordsIndex.get(kw1); //Arraylist consisting of all the occurrences for keyword 1
		ArrayList<Occurrence> freqKW2 = keywordsIndex.get(kw2); //Arraylist consisting of all the occurrences for keyword 2

		if(freqKW1 == null){
			for(int i =0; i<5;i++){
				documents.add(freqKW2.get(i).document);
			}
			return documents;
		}
		else if(freqKW2 == null){
			for(int i =0; i<5;i++){
				documents.add(freqKW1.get(i).document);
			}
			return documents;
		}
		/*
		 * Since I have the array list of occurrences for each key word and they are already organized in descending order,
		 * I then compare each array index from greatest to smallest between the two arrays and place the higher frequency in
		 * the new array, which I will return at the end of the method.
		 */

		int i = 0;

		while(freqKW1.size() != 0 || freqKW2.size()!=0 && i<5){

			if(freqKW1.size() == 0){
				if(!documents.contains(freqKW2.get(0).document)){
					documents.add(freqKW2.get(0).document);
					freqKW2.remove(0);
				}
				else{
					freqKW2.remove(0);
				}
			}
			else if(freqKW2.size() ==0){
				if(!documents.contains(freqKW1.get(0).document)){
					documents.add(freqKW1.get(0).document);
					freqKW1.remove(0);
				}
				else{
					freqKW1.remove(0);
				}
			}
			else if(freqKW1.size() != 0 && freqKW2.size()!=0 && freqKW1.get(0).frequency >= freqKW2.get(0).frequency && !documents.contains(freqKW1.get(0).document)){
				documents.add(freqKW1.get(0).document);
				freqKW1.remove(0);
			}
			else if (freqKW1.size() != 0 && freqKW2.size()!=0 && freqKW1.get(0).frequency < freqKW2.get(0).frequency && !documents.contains(freqKW2.get(0).document)){
				documents.add(freqKW2.get(0).document);
				freqKW2.remove(0);
			}

			i++;
		}

		return documents;
	}
}

