using Chat.Controllers;
using Chat.Hubs;
using Microsoft.AspNet.SignalR;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace Chat.Controllers
{
    public class ChatSignalRController : ApiControllerWithHub<MyHub>
    {
        public IHttpActionResult Get(string customerId)
        {
            
            


            var cosa = Hub.Clients.Group(customerId);
            if (cosa != null)
                return Ok(cosa);
            else
                return Ok();
        }
    }
}
