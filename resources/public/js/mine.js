function updateStats(){
    $.ajax({
      url: "stats",
    }).done(function(response) {
      response = JSON.parse(response);
      $.each(response.data, function (idx, item){
        var element =  $("#"+item.id);
        var width =  Math.min(item.value / element.attr("aria-valuemax") * 100, 100);

        element.css("width", width + "%")
        element.text(item.value)
        element.attr("class","");
        if(width > 70){
            element.attr("class","progress-bar progress-bar-danger")
        } else if (width > 40){
            element.attr("class","progress-bar progress-bar-warning")
        } else {
            element.attr("class","progress-bar progress-bar-success")
        }

      });
    });
}

function updateHealth(){
    $.ajax({
      url: "health",
    }).done(function(response) {
      response = JSON.parse(response);
      $.each(response.data, function (idx, item){
        var element =  $("#"+item.id);
        var health =  item.value;
        element.attr("class","");
        if(health){
            element.attr("class","label label-success")
        } else {
            element.attr("class","label label-danger")
        }
      });
    });
}

setInterval(updateStats, 2000);
setInterval(updateHealth, 2000);