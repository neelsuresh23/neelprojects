/*
flightPlan.java
Author: Neel Suresh

This program is used to find the top 3 optimized routes to travel from one city to another based on user preferences for time or cost. 

How to run:
1. Specify a list of all flights in a .txt file. The first line details the total number of flights. 
All subsequent lines are flights formatted as such: <Departure city>|<Arrival city>|Cost>|<Time>

2. Specify a list of routes to search for. The first line details the total number of routes. 
All subsequent lines are flights formatted as such: <Departure city>|<Arrival city>|<T/C (Time or Cost)>

3. Run program with 2 arguments. First is .txt file of flights. Second is .txt file of routes to search for. 
Results will be written to OutputFile.txt

Example:
java lightPlan FlightDataFile.txt PathsToCalculateFile.txt

*/
package CS3345Project1;
import java.util.LinkedList;
import java.io.File;  
import java.io.FileNotFoundException; 
import java.util.Scanner; 
import java.util.Stack;
import java.io.FileWriter;
import java.io.IOException;
public class flightPlan {
	private static LinkedList<City> cityList= new LinkedList<City>();
	private static LinkedList<FlightRequest> requestList= new LinkedList<FlightRequest>();
	public static void main(String[] args)
	{		
		createCityList(args[0]);//initialize the linked list of cities
		createRequestList(args[1]);//initialize the linked list of flight requests
		
		for(FlightRequest f: requestList)//iterate through every request in the list
		{
			Stack<Flight> shortestPath= findShortestPath(f);//find the 1st shortest path, and store the path in a Stack
			for(Flight flight: shortestPath)//backtrack through the shortest path we just found. 
			{
				remove(flight);//For every flight in that path, try removing it, and running findShortestPath again to see if it produces another path 
				findShortestPath(f);//once we've backtracked through the entire path, we will have found the 2nd and 3rd shortest path, if it exists
				add(flight);//add flight back before next iteration
			}
		}
		writeResult();//after we go through all requested flights, write to the output file

	}
	public static void add(Flight f)//adds a flight to cityList
	{
		for(City c: cityList)
		{
			if(c.getName().equals(f.getOriginCity().getName()))
			{
				c.getFlightList().add(f);
			}
		}
	}
	public static void remove(Flight f)//removes a flight from cityList
	{
		for(City c: cityList)
		{
			if(c.getName().equals(f.getOriginCity().getName()))
			{
				c.getFlightList().remove(f);
			}
		}
	}
	
	
	public static Stack<Flight> findShortestPath(FlightRequest flightRequest)//given a flight request, run Dijkstra's to determine shortest path to destination
	{
		
	      String origin= flightRequest.getOrigin();
	      String destination= flightRequest.getDestination();
	      String timeOrCost= flightRequest.getTimeOrCost();
		  Stack<Flight> shortestPath= new Stack<Flight>();
		  resetCityData(origin);	
		  for(int j=0; j<cityList.size(); j++)//iterate through every vertex
		  {
			  City u=minDist(timeOrCost);//find vertex with lowest distance
			  u.setVisited(true);
			  LinkedList<Flight> connectingFlights= u.getFlightList();				

			  for(int v=0; v<connectingFlights.size(); v++)//do relaxation for all connecting vertices
			  {
				  Flight f= connectingFlights.get(v);
				  City c= f.getDestinationCity();
				  if(!c.getVisited() && timeOrCost.equals("T") && 
						  u.getShortestTime()!=Integer.MAX_VALUE && 
						  u.getShortestTime()+f.getTime()<c.getShortestTime())
				  {
					  c.setShortestTime(u.getShortestTime()+f.getTime());
					  c.setShortestCost(u.getShortestCost()+f.getCost());
					  c.setPath(f);
				  }
				  else if(!c.getVisited() && timeOrCost.equals("C") && 
						  u.getShortestCost()!=Integer.MAX_VALUE && 
						  u.getShortestCost()+f.getCost()<c.getShortestCost())
				  {

					  c.setShortestTime(u.getShortestTime()+f.getTime());
					  c.setShortestCost(u.getShortestCost()+f.getCost());
					  c.setPath(f);


				  }
					
			  }

			  if(u.getName().equals(destination))//create a stack with all the flights that lead from the origin to destination
			  {
				  while (u.getPath() !=null)
				  {
					  shortestPath.push(u.getPath());
					  u=u.getPath().getOriginCity();
				  }
			  }
		  }
		  for(City c: cityList)//add the path and its details to the FlightRequest object to see if it qualifies as a top 3 path
		  {
				if(c.getName().equals(destination) && timeOrCost.equals("T"))
				{
					flightRequest.add(c.getShortestTime(), c.getShortestCost(), shortestPath);
				}
				else if(c.getName().equals(destination) && timeOrCost.equals("C"))
				{
					flightRequest.add(c.getShortestTime(), c.getShortestCost(), shortestPath);

				}
				
			}
		  return shortestPath;

	}
	
	public static void resetCityData(String origin)//resets the cost, time, visited, and path of each city. Called before every time we run Dijkstra's
	{
		for(int i=0; i<cityList.size(); i++)
		{
			City c= cityList.get(i);
			c.setShortestCost(Integer.MAX_VALUE);
			c.setShortestTime(Integer.MAX_VALUE);
			c.setVisited(false);
			c.setPath(null);

			if(c.getName().equals(origin))
			{
				c.setShortestCost(0);
				c.setShortestTime(0);
			}


		}

	}
	public static City minDist(String timeOrCost)//helper method for findShortestPath, returns the City that is accessible with the lowest cost
	{
		int min= Integer.MAX_VALUE;
		int minIndex=-1;
		City minCity= cityList.get(0);
		for(int i=0; i<cityList.size(); i++)
		{
			City c= cityList.get(i);
			if(!c.getVisited() && timeOrCost.equals("T") && c.getShortestTime()<min)
			{
				
				min=c.getShortestTime();
				minCity= c;
				minIndex=i;
			}
			else if(!c.getVisited() && timeOrCost.equals("C") && c.getShortestCost()<min)
			{
				min=c.getShortestCost();
				minCity= c;
				minIndex=i;
				
			}
			
		}
		return minCity;
	}
	
	public static void writeResult()//writes the final result to OutputFile.txt
	{
		try
		{
			FileWriter writer = new FileWriter("OutputFile.txt");

			for(FlightRequest f: requestList)//loop through every flight request
			{

				writer.write("Flight "+ f.getCount()+": "+f.getOrigin()+", "+f.getDestination());
				if(f.getTimeOrCost().equals("T"))
				{
					writer.write(" (Time)\n");
				}
				else if(f.getTimeOrCost().equals("C"))
				{
					writer.write(" (Cost)\n");
				}
				for(int i=0; i<3; i++)//loop through the top 3 paths
				{
					Stack<Flight> path= f.getPath()[i];
					if(path !=null && f.getShortestCost()[i] !=Integer.MAX_VALUE)//make sure the path is not empty and that the cost not the default set max value
					{
						writer.write("Path "+(i+1)+": ");
	
						while(!path.empty())
						{
							Flight flight= path.pop();
							writer.write(flight.getOriginCity().getName()+" -> ");//write to file
							if(flight.getDestinationCity().getName().equals(f.getDestination()))
							{
								writer.write(flight.getDestinationCity().getName()+". ");
							}
						}
	
						writer.write("Time: "+f.getShortestTime()[i]+ " Cost: "+f.getShortestCost()[i]+"\n");
					}
				}
				writer.write("\n");

			}
			writer.close();

		} 

		catch (IOException e) 
		{
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	
	public static void createRequestList(String file2Name)//reads the file PathsToCalculateFile.txt and creates the requestList linkedlist
	{
		
		try
		{
			File file = new File(file2Name);
		    Scanner scanner = new Scanner(file);
		    scanner.nextLine();
		    int count=1;
		    while (scanner.hasNextLine())
		    {
		    	String line = scanner.nextLine();
	
		        String origin= line.substring(0, line.indexOf('|'));
		        line=line.substring(line.indexOf('|')+1);
		        String destination= line.substring(0, line.indexOf('|'));
		        line=line.substring(line.indexOf('|')+1);
		        String weight= line;
		        
		        FlightRequest f= new FlightRequest(origin, destination, weight, count);
		        requestList.add(f);
		        count++;
		
	      }
		    
		
	       
		}  
	      catch (FileNotFoundException e) 
	      {
			      System.out.println("An error occurred.");
			      e.printStackTrace();
			
		  }
		
	}
	public static City findCity(String name)//helper method for createCityList, returns the City object when provided its name
	{
		for(City c: cityList)
		{
			if(c.getName().equals(name))
				return c;
		}
		return null;
	}
	public static void createCityList(String file1Name)//initializes the cityList linkedlist
	{
		
		 
		 try
		 {
		      File file = new File(file1Name);
		      Scanner scanner = new Scanner(file);
		      scanner.nextLine();
		      while (scanner.hasNextLine())//read every line of the text file
		      {
		        String line = scanner.nextLine();

		        String origin= line.substring(0, line.indexOf('|'));
		        line=line.substring(line.indexOf('|')+1);
		        String destination= line.substring(0, line.indexOf('|'));
		        line=line.substring(line.indexOf('|')+1);
		        String costString= line.substring(0, line.indexOf('|'));
		        line=line.substring(line.indexOf('|')+1);
		        String timeString= line;
		        
		        int cost=Integer.parseInt(costString);
		        int time=Integer.parseInt(timeString);

		        
		        boolean isOriginInList=false;
		        boolean isDestinationInList=false;

		        for(City c: cityList)//traverse through the cityList
		        {
		        	if(c.getName().equals(origin))//if the city already exists in the list, add the flight
		        	{
		        		isOriginInList=true;
		        		c.addFlight(new Flight(origin, destination, cost, time));
		        	}
		        	if(c.getName().equals(destination))
		        	{
		        		isDestinationInList=true;
		        		c.addFlight(new Flight(destination, origin, cost, time));
		        	}

		        }
		        
		        if(!isOriginInList)//if the corresponding boolean value is still set to false, then the city will be added to the list afterwards
		        {
		        	City c= new City(origin);
	        		c.addFlight(new Flight(origin, destination, cost, time));
		        	cityList.add(c);
		        }
		        if(!isDestinationInList)
		        {
		        	City c= new City(destination);
	        		c.addFlight(new Flight(destination,origin, cost, time));
		        	cityList.add(c);
		        }
		        
		       

		      }
		      scanner.close();
		      for(City c: cityList)//traverse one more time to initialize the origin and destination variable 
		        {
		    	  	
		    	  	LinkedList<Flight> flights= c.getFlightList();
		    	  	
		    	  	for(Flight f: flights)
		    	  	{
		    	  		f.setOriginCity(c);
		    	  		f.setDestinationCity(findCity(f.getDestination()));
		    	  	}

		        }
		 }
		 catch (FileNotFoundException e) 
		 {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		
		 }
	}

}

class Flight//this class represents a flight from one city to another. It is essentially the edges of the graph
{
	private String origin, destination;//names of the flight's origin and destination
	private int cost, time;//cost and time of the flight
	private City originCity, destinationCity;//Objects pointing to the origin and destination
	
	public Flight(String origin, String destination, int cost, int time)
	{
		this.origin=origin;
		this.destination=destination;
		this.cost=cost;
		this.time=time;
		originCity=null;
		destinationCity=null;
	}
	//getter and setter methods for all variables
	public String getDestination()
	{
		return destination;
	}
	public void setDestinationCity(City c)
	{
		destinationCity=c;
	}
	public City getDestinationCity()
	{
		return destinationCity;
	}
	public void setOriginCity(City c)
	{
		originCity=c;
	}
	public City getOriginCity()
	{
		return originCity;
	}
	public int getCost()
	{
		return cost;
	}
	public int getTime()
	{
		return time;
	}
}

class City//This class represents a City in which flights can travel between. It is essentially the vertices of the graph
{
	private String name; 
	private LinkedList<Flight> flightList= new LinkedList<Flight>();//each city has a linkedlist of its outgoing flights
	//the following are used in performing dijkstra's algorithm
	private int shortestTime, shortestCost;//holds the current shortest time/cost
	private boolean visited;//whether the city has been visited yet
	private Flight path;//the previous node in the current shortest path
	
	
	public City(String name)
	{
		this.name=name;
		shortestTime= Integer.MAX_VALUE;
		shortestCost= Integer.MAX_VALUE;
		visited= false;
		path=null;
	}
	//getter and setter methods for all variables
	public void addFlight(Flight f)
	{
		flightList.add(f);
		
	}
	public void setPath(Flight f)
	{
		path=f;
	}
	public Flight getPath()
	{
		return path;
	}
	public String getName()
	{
		return name;
	}
	
	public LinkedList<Flight> getFlightList()
	{
		return flightList;
	}
	
	public int getShortestTime()
	{
		return shortestTime;
	}
	
	public void setShortestTime(int i)
	{
		shortestTime=i;
	}
	public int getShortestCost()
	{
		return shortestCost;
	}
	public void setShortestCost(int i)
	{
		shortestCost=i;
	}
	public boolean getVisited()
	{
		return visited;
	}
	public void setVisited(boolean i)
	{
		visited=i;
 
	}	
}
@SuppressWarnings("unchecked")
class FlightRequest//this class is used to hold a requested flight path, as well as data to achieve the top 3 shortest paths
{
	String origin, destination, timeOrCost;
	Stack<Flight> []top3Path;//each stack holds the path between the origin and destination
	int [] top3Time;//holds the current top 3 shortest times
	int[] top3Cost;//holds the current top 3 shortest cost
	int count; 
	public FlightRequest(String origin, String destination, String timeOrCost, int count)
	{
		this.origin= origin;
		this.destination=destination;
		this.timeOrCost=timeOrCost;
		top3Path= new Stack[3];
		top3Time= new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};//set defaults to max value unless otherwise found
		top3Cost= new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
		this.count= count;
	}
	//getter and setter methods
	public int getCount()
	{
		return count; 
	}
	public int[] getShortestTime()
	{
		return top3Time;
	}
	public int[] getShortestCost()
	{
		return top3Cost;
	}
	public Stack<Flight>[] getPath()
	{
		return top3Path;
	}
	public void add(int time, int cost, Stack<Flight> path)
	{
		if(timeOrCost.equals("T"))//checks if the given path qualifies as a top 3 path. If it is, save it. Otherwise, discard
		{
			if(time<=top3Time[0])
			{
				top3Time[0]= time;
				top3Cost[0]= cost;
				top3Path[0]=path;

			}
			else if(time<=top3Time[1])
			{
				top3Time[1]= time;
				top3Cost[1]= cost;
				top3Path[1]=path;

			}
			else if(time<=top3Time[2])
			{
				top3Time[2]= time;
				top3Cost[2]= cost;
				top3Path[2]=path;

			}
			
		}
		
		else if(timeOrCost.equals("C"))
		{
			if(cost<=top3Cost[0])
			{
				top3Time[0]= time;
				top3Cost[0]= cost;
				top3Path[0]=path;

			}
			else if(cost<=top3Cost[1])
			{
				top3Time[1]= time;
				top3Cost[1]= cost;
				top3Path[1]=path;

			}
			else if(cost<=top3Cost[2])
			{
				top3Time[2]= time;
				top3Cost[2]= cost;
				top3Path[2]=path;

			}
		}

	}
	public String getOrigin()
	{
		return origin;
	}
	public String getDestination() 
	{
		return destination;
	}
	public String getTimeOrCost()
	{
		return timeOrCost;
	}
	
}