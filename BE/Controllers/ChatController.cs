using Microsoft.Web.WebSockets;
using System;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web;
using System.Web.Http;

namespace Chat.Controllers
{
    public class ChatController : ApiController
    {
        public HttpResponseMessage Get(string username)
        {
            try
            {
                HttpContext.Current.AcceptWebSocketRequest(new ChatWebSocketHandler(username));
                return Request.CreateResponse(HttpStatusCode.SwitchingProtocols);
            }
            catch (Exception ex) {
                SqlConnection con = new SqlConnection(System.Configuration.ConfigurationManager.ConnectionStrings["CS"].ConnectionString);
                SqlCommand cmd = new SqlCommand("catLog_Insert", con);
                cmd.CommandType = System.Data.CommandType.StoredProcedure;
                cmd.Parameters.AddWithValue("@Sistema", "CHAT");
                cmd.Parameters.AddWithValue("@Exception", ex.Message);
                cmd.Parameters.AddWithValue("@Usuario", "GCORONAD");
                con.Open();
                cmd.ExecuteNonQuery();
                con.Close();

                return Request.CreateResponse(HttpStatusCode.InternalServerError, ex.Message);
            }
        }

        class ChatWebSocketHandler : WebSocketHandler
        {
            private static WebSocketCollection _chatClients = new WebSocketCollection();
            private string _username;

            public ChatWebSocketHandler(string username)
            {
                _username = username;
            }

            public override void OnOpen()
            {
                _chatClients.Add(this);
            }

            public override void OnMessage(string message)
            {
                _chatClients.Broadcast(_username + ": " + message);
            }
        }
    }
}
