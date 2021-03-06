import Turbolinks
import UIKit

class WebViewController: Turbolinks.VisitableViewController {
    
    var manager: RNTurbolinksManager!
    var route: TurbolinksRoute!
    var customView: UIView?
    var selectorHandleLeftButtonPress: Selector = #selector(handleLeftButtonPress)
    var selectorPresentActions: Selector = #selector(presentActionsGeneric)
    
    convenience required init(manager: RNTurbolinksManager, route: TurbolinksRoute) {
        self.init(url: route.url!)
        self.manager = manager
        self.route = route
        self.renderLoadingStyle()
        self.renderActions()
        self.renderBackButton()
        self.renderLeftButton()
    }

    func renderComponent() {
        customView = RCTRootView(bridge: manager.bridge, moduleName: route.component, initialProperties: route.passProps)
        customView!.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(customView!)
        installErrorViewConstraints()
    }
    
    func reload() {
        customView?.removeFromSuperview()
        reloadVisitable()
    }
    
    fileprivate func installErrorViewConstraints() {
        view.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "H:|[view]|", options: [], metrics: nil, views: [ "view": customView! ]))
        view.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "V:|[view]|", options: [], metrics: nil, views: [ "view": customView! ]))
    }
    
    fileprivate func renderLoadingStyle() {
        visitableView.activityIndicatorView.backgroundColor = manager.loadingBackgroundColor ?? .white
        visitableView.activityIndicatorView.color = manager.loadingColor ?? .gray
    }
    
    fileprivate func handleVisitCompleted() {
        let javaScriptString = "document.documentElement.outerHTML"
        visitableView.webView!.evaluateJavaScript(javaScriptString, completionHandler: { (document, error) in
            self.manager.handleVisitCompleted(url: self.visitableURL, source: document as? String)
        })
    }
    
    override func visitableDidRender() {
        super.visitableDidRender()
        renderTitle()
        handleVisitCompleted()
    }
    
}

extension WebViewController: GenricViewController {
    
    func handleTitlePress() {
        manager.handleTitlePress(URL: visitableURL, component: route.component)
    }
    
    @objc func handleLeftButtonPress() {
        manager.handleLeftButtonPress(URL: visitableURL, component: nil)
    }
    
    @objc func presentActionsGeneric(_ sender: UIBarButtonItem) {
        self.presentActions(sender)
    }
}


