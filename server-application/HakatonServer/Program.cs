using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Fleck;
using MongoDB.Driver;
using MongoDB.Bson;
using MongoDB.Shared;
using Newtonsoft.Json.Serialization;
using Newtonsoft.Json;
using MongoDB.Driver.Builders;

namespace HakatonServer
{
    enum TYPE
    {
        DATA = 0,
        GET_ID = 1,
        INSERT_SHELTER = 2,
        GET_SHELTER = 3,
        GET_ROUTES = 4,
        UPDATE_SHELTER = 8,
        INSERT_ROUTE = 9
    }

    enum RESPONSE
    {
        SUCCESS = 0,
        FAILURE = 1
    }

    class Program
    {
        static List<IWebSocketConnection> sockets = new List<IWebSocketConnection>();

        static void Main(string[] args)
        {
            var server = new WebSocketServer("ws://0.0.0.0:8181");
            server.Start(socket =>
            {
                socket.OnOpen = () =>
                {
                    Console.WriteLine("Client connected");
                    Console.WriteLine("IP: " + socket.ConnectionInfo.ClientIpAddress);
                    sockets.Add(socket);
                };

                socket.OnClose = () =>
                {
                    Console.WriteLine("Client disconnected");
                    sockets.Remove(socket);
                };

                socket.OnMessage = msg =>
                {
                    Console.WriteLine(msg);

                    dynamic obj = JsonConvert.DeserializeObject<dynamic>(msg);
                    int type = obj.type;

                    switch (type)
                    {
                        case (int)TYPE.DATA:
                            getData(socket);
                            break;
                        case (int)TYPE.GET_ID:
                            getId(socket);
                            break;
                        case (int)TYPE.INSERT_SHELTER:
                            insertShelter(socket, Convert.ToString(obj.data));
                            break;
                        case (int)TYPE.GET_SHELTER:
                            getShelter(socket, (string)obj.data);
                            break;
                        case (int)TYPE.UPDATE_SHELTER:
                            updateShelter(socket, Convert.ToString(obj.data));
                            break;
                        case (int)TYPE.INSERT_ROUTE:
                            insertRoute(socket, Convert.ToString(obj.data));
                            break;
                        case (int)TYPE.GET_ROUTES:
                            getRoutes(socket);
                            break;
                    }
                };
            });

            var input = Console.ReadLine();

            while (input != "exit")
            {
                input = Console.ReadLine();
            }

            Console.WriteLine("bye");
        }

        static void getData(IWebSocketConnection socket)
        {
            var connectionString = "mongodb://localhost/?safe=true";
            var mongoClient = new MongoClient(connectionString);
            var dbserver = mongoClient.GetServer();
            var db = dbserver.GetDatabase("refaid");
            var collection = db.GetCollection<BsonDocument>("shelters");

            var cursor = collection.FindAll();
            var json = cursor.ToJson(new MongoDB.Bson.IO.JsonWriterSettings());



            socket.Send("{ type: 0, code: 0, message: 'done', data: " + json + " }");

            Console.WriteLine(json);
        }

        static void getId(IWebSocketConnection socket)
        {
            var id = Guid.NewGuid().ToString();
            var json = "[{ userId: \"" + id + "\" }]";

            socket.Send("{ type: 0, code: 0, message: 'done', data: " + json + " }");

            Console.WriteLine(json);
        }

        static void insertShelter(IWebSocketConnection socket, string data)
        {
            var connectionString = "mongodb://localhost/?safe=true";
            var mongoClient = new MongoClient(connectionString);
            var dbserver = mongoClient.GetServer();
            var db = dbserver.GetDatabase("refaid");
            var collection = db.GetCollection<BsonDocument>("shelters");

            collection.Insert(BsonDocument.Parse(data));

            foreach (var s in sockets)
                if (s != socket)
                    s.Send(data);
        }

        static void insertGroup(IWebSocketConnection socket, string data)
        {
            var connectionString = "mongodb://localhost/?safe=true";
            var mongoClient = new MongoClient(connectionString);
            var dbserver = mongoClient.GetServer();
            var db = dbserver.GetDatabase("refaid");
            var collection = db.GetCollection<BsonDocument>("groups");

                
        }

        static void getShelter(IWebSocketConnection socket, string id)
        {
            var connectionString = "mongodb://localhost/?safe=true";
            var mongoClient = new MongoClient(connectionString);
            var dbserver = mongoClient.GetServer();
            var db = dbserver.GetDatabase("refaid");
            var collection = db.GetCollection<BsonDocument>("shelters");

            var cursor = collection.Find(Query.EQ("_id", ObjectId.Parse(id)));
            var json = cursor.ToJson(new MongoDB.Bson.IO.JsonWriterSettings());

            socket.Send("{type: 3, code: 0, message: 'done', data: " + json + "}");

            Console.WriteLine(json);
        }

        static void updateShelter(IWebSocketConnection socket, string data)
        {
            var connectionString = "mongodb://localhost/?safe=true";
            var mongoClient = new MongoClient(connectionString);
            var dbserver = mongoClient.GetServer();
            var db = dbserver.GetDatabase("refaid");
            var collection = db.GetCollection<BsonDocument>("shelters");

            dynamic obj = JsonConvert.DeserializeObject<dynamic>(data);

            //var id = ObjectId.Parse(obj._id);
            //var query = Query.EQ("_id", id);
            var name = (string)obj.name;
            var query = Query.EQ("name", name);

            var response = collection.FindAs<BsonDocument>(query);
            var result = response.Single();

            result.Set("name", (String)obj.name);
            result.Set("type", (String)obj.type);
            result.Set("resources", (String)obj.resources);

            collection.Save(result);
        }


        static void insertRoute(IWebSocketConnection socket, string data)
        {
            var connectionString = "mongodb://localhost/?safe=true";
            var mongoClient = new MongoClient(connectionString);
            var dbserver = mongoClient.GetServer();
            var db = dbserver.GetDatabase("refaid");
            var collection = db.GetCollection<BsonDocument>("routes");

            dynamic obj = JsonConvert.DeserializeObject<dynamic>(data);

            collection.Insert(BsonDocument.Parse(data));

            foreach (var s in sockets)
                getRoutes(s);
        }


        static void getRoutes(IWebSocketConnection socket)
        {
            var connectionString = "mongodb://localhost/?safe=true";
            var mongoClient = new MongoClient(connectionString);
            var dbserver = mongoClient.GetServer();
            var db = dbserver.GetDatabase("refaid");
            var routes = db.GetCollection<BsonDocument>("routes");
            var shelters = db.GetCollection<BsonDocument>("shelters");

            var cursor = routes.FindAll();
            List<String> jsonList = new List<String>();
            if (cursor.Count() > 0)
            {
                foreach (var doc in cursor)
                {
                    BsonDocument d = new BsonDocument();

                    ObjectId fromId = ObjectId.Parse(doc.GetValue("fromId").ToString());
                    ObjectId toId = ObjectId.Parse(doc.GetValue("toId").ToString());

                    var query = Query.EQ("_id", fromId);
                    var shelter = shelters.Find(query).Single();

                    d.Set("departure", doc.GetValue("departure"));
                    d.Set("seats", doc.GetValue("seats"));
                    d.Set("shelter", shelter);

                    jsonList.Add(d.ToJson(new MongoDB.Bson.IO.JsonWriterSettings()));

                    d = new BsonDocument();

                    query = Query.EQ("_id", toId);
                    shelter = shelters.Find(query).Single();

                    d.Set("departure", doc.GetValue("departure"));
                    d.Set("seats", doc.GetValue("seats"));
                    d.Set("shelter", shelter);

                    jsonList.Add(d.ToJson(new MongoDB.Bson.IO.JsonWriterSettings()));
                }

                var json = "[ ";
                for (int i = 0; i < jsonList.Count - 1; ++i)
                    json += jsonList.ElementAt(i) + ", ";
                json += jsonList.ElementAt(jsonList.Count - 1) + " ]";

                socket.Send("{type: 4, code: 0, message: 'done', data: " + json + "}");
            }
            else
                socket.Send("{type:4, code:0, message: 'done', data: [] }");
        }


    }
        
}
