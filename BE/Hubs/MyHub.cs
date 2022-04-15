using Microsoft.AspNet.SignalR;
using System;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Linq;
using System.Web;
using System.Threading.Tasks;

namespace Chat.Hubs
{
    public class MyHub : Hub
    {
        public void BroadCast()
        {
            Clients.All.BroadCast("hola mundo");
        }

        public string Message(string connectionId, string message) {
            //Envia mensaje a todos menos a la correspondiente conexión
            Clients.AllExcept(connectionId).BroadCast(message);
            //Envia mensaje a todos menos a la correspondiente conexión
            //Clients.Others().BroadCast(message);

            return "exito";
        }

        public override Task OnConnected()
        {
            var headers = Context.Request.Headers.AsEnumerable();
            var idSolicitud = Context.Request.Headers.Get("IdSolicitud");
            var idEmpleado = Context.Request.Headers.Get("IdEmpleado");

           

            //Groups.Add(Context.ConnectionId, idSolicitud);
            /*
            Clients.Client(Context.ConnectionId).BroadCast("Hola mundo 2");
            Clients.AllExcept(Context.ConnectionId).BroadCast("Hola mundo nuevo");*/
            return base.OnConnected();
        }

        public override Task OnDisconnected(bool stopCalled)
        {
            return base.OnDisconnected(stopCalled);
        }

        public override Task OnReconnected()
        {
            return base.OnReconnected();
        }
    }
}