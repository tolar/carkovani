$ ->
  wsUrl = $("#dashboard").data("ws-url")
  if wsUrl
    ws = new WebSocket wsUrl
    ws.onmessage = (event) ->
      message = JSON.parse event.data
      console.log message.type
      switch message.type
        when "data"
          updateDashboard(message)


  $("#create").click
    handler: (event) ->
      $.ajax
        url: "/create/" + $("#newName").val()
        dataType: "json"
        method: "POST"
      success: (data) ->
      error: (jqXHR, textStatus, error) ->
        alert("Error: " + JSON.parse(jqXHR.responseText).error);
#        detailsHolder = $(this).find(".details-holder")
#        detailsHolder.empty()
#        detailsHolder.append($("<h2>").text("Error: " + JSON.parse(jqXHR.responseText).error))



  updateDashboard = (message) ->
    $("#dashboard").empty()

    for item in message.items

      commas = ''
      name = $("<strong>").text(item.name)
      console.log("SCORE:" + item.score)
      spanCommas = []
      for i in [1 .. item.score] by 1
        commas = commas + 'I'
        console.log(i)
        spanComma = $("<div style=\"width:10px;height:20px\">").addClass("chart-holder")
        spanComma.plot(
          [[[0, 0], [1, 1]] ],
          {
            yaxis: { show: false },
            xaxis: { show: false },
            grid: { show: false}
          }
        ).data("plot")
        spanCommas.push(spanComma)

      tr = $("<tr>")
      spanName = $("<span>").append(name)





      # button Remove
      btnRemove = $("<button>")
      btnRemove.addClass("btn btn-default btn-xs").append($("<span>").addClass("glyphicon glyphicon-remove"))
      btnRemove.click
        data: {name: item.name}
        handler: (event) ->
          event.preventDefault()
          ws.send(JSON.stringify({operation: 'remove', name:  event.data.name}))

      # button Increment
      btnInc = $("<button>")
      btnInc.addClass("btn btn-default btn-xs").append($("<span>").addClass("glyphicon glyphicon-menu-up"))
      btnInc.click
        data: {name: item.name}
        handler: (event) ->
          event.preventDefault()
          ws.send(JSON.stringify({operation: 'increment', name:  event.data.name}))

      # button Decrement
      btnDec = $("<button>")
      btnDec.addClass("btn btn-default btn-xs").append($("<span>").addClass("glyphicon glyphicon-menu-down"))
      btnDec.click
        data: {name: item.name}
        handler: (event) ->
          event.preventDefault()
          ws.send(JSON.stringify({operation: 'decrement', name:  event.data.name}))

      tdBtnsName = $("<td>")
      tdBtnsName.addClass("col-md-4")
      btnGroup = $("<div>")
      btnGroup.addClass("btn-group")
      btnGroup.append(btnRemove, btnInc, btnDec)
      tdBtnsName.append(btnGroup)
      tdBtnsName.append($("<span>&nbsp;&nbsp;</span>"))
      tdBtnsName.append(spanName)
      tdCommas = $("<td>")
      tdCommas.addClass("col-md-8")
      tdCommas.append(spanCommas)

      tr.append(tdBtnsName, tdCommas)

      $("#dashboard").append((tr))

      $("#dashboard").append($("<br>"))


#    switch message.type
#      when "stockhistory"
#        populateStockHistory(message)
#      when "stockupdate"
#        updateStockChart(message)
#      else
#        console.log(message)
#
#  $("#addsymbolform").submit (event) ->
#    event.preventDefault()
#    # send the message to watch the stock
#    ws.send(JSON.stringify({symbol: $("#addsymboltext").val()}))
#    # reset the form
#    $("#addsymboltext").val("")

#getPricesFromArray = (data) ->
#  (v[1] for v in data)
#
#getChartArray = (data) ->
#  ([i, v] for v, i in data)
#
#getChartOptions = (data) ->
#  series:
#    shadowSize: 0
#  yaxis:
#    min: getAxisMin(data)
#    max: getAxisMax(data)
#  xaxis:
#    show: false
#
#getAxisMin = (data) ->
#  Math.min.apply(Math, data) * 0.9
#
#getAxisMax = (data) ->
#  Math.max.apply(Math, data) * 1.1
#
#populateStockHistory = (message) ->
#  chart = $("<div>").addClass("chart").prop("id", message.symbol)
#  chartHolder = $("<div>").addClass("chart-holder").append(chart)
#  chartHolder.append($("<p>").text("values are simulated"))
#  detailsHolder = $("<div>").addClass("details-holder")
#  flipper = $("<div>").addClass("flipper").append(chartHolder).append(detailsHolder).attr("data-content", message.symbol)
#  flipContainer = $("<div>").addClass("flip-container").append(flipper).click (event) ->
#    handleFlip($(this))
#  $("#stocks").prepend(flipContainer)
#  plot = chart.plot([getChartArray(message.history)], getChartOptions(message.history)).data("plot")
#
#updateStockChart = (message) ->
#  if ($("#" + message.symbol).size() > 0)
#    plot = $("#" + message.symbol).data("plot")
#    data = getPricesFromArray(plot.getData()[0].data)
#    data.shift()
#    data.push(message.price)
#    plot.setData([getChartArray(data)])
#    # update the yaxes if either the min or max is now out of the acceptable range
#    yaxes = plot.getOptions().yaxes[0]
#    if ((getAxisMin(data) < yaxes.min) || (getAxisMax(data) > yaxes.max))
## reseting yaxes
#      yaxes.min = getAxisMin(data)
#      yaxes.max = getAxisMax(data)
#      plot.setupGrid()
#    # redraw the chart
#    plot.draw()
#
#handleFlip = (container) ->
#  if (container.hasClass("flipped"))
#    container.removeClass("flipped")
#    container.find(".details-holder").empty()
#  else
#    container.addClass("flipped")
#    # fetch stock details and tweet
#    $.ajax
#      url: "/sentiment/" + container.children(".flipper").attr("data-content")
#      dataType: "json"
#      context: container
#      success: (data) ->
#        detailsHolder = $(this).find(".details-holder")
#        detailsHolder.empty()
#        switch data.label
#          when "pos"
#            detailsHolder.append($("<h4>").text("The tweets say BUY!"))
#            detailsHolder.append($("<img>").attr("src", "/assets/images/buy.png"))
#          when "neg"
#            detailsHolder.append($("<h4>").text("The tweets say SELL!"))
#            detailsHolder.append($("<img>").attr("src", "/assets/images/sell.png"))
#          else
#            detailsHolder.append($("<h4>").text("The tweets say HOLD!"))
#            detailsHolder.append($("<img>").attr("src", "/assets/images/hold.png"))
#      error: (jqXHR, textStatus, error) ->
#        detailsHolder = $(this).find(".details-holder")
#        detailsHolder.empty()
#        detailsHolder.append($("<h2>").text("Error: " + JSON.parse(jqXHR.responseText).error))
#    # display loading info
#    detailsHolder = container.find(".details-holder")
#    detailsHolder.append($("<h4>").text("Determining whether you should buy or sell based on the sentiment of recent tweets..."))
#    detailsHolder.append($("<div>").addClass("progress progress-striped active").append($("<div>").addClass("bar").css("width", "100%")))
