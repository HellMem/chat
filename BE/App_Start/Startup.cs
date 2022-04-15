using Microsoft.Owin;
using Owin;

[assembly: OwinStartup(typeof(Chat.App_Start.Startup))]
namespace Chat.App_Start
{
    public class Startup
    {
        public void Configuration(IAppBuilder app)
        {
            app.MapSignalR();
        }
    }
}