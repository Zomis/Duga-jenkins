package net.zomis.jenkins.duga.DugaJobProperty

def f = namespace(lib.FormTagLib);
def c = namespace(lib.CredentialsTagLib);

println "DFUHLGSIWEUHEERHUIGLGHRDELGUXRDHG"

f.optionalBlock(name: 'hasDugaNotifier', title: 'Duga Chat', checked: instance != null) {
    f.entry(field: 'remoteUrl', title: 'Remote URL') {
        f.textbox()
    }

    f.entry(field: 'apiKey', title: 'API Key') {
        f.textbox()
    }

    f.entry(field: 'credentials', title: 'Bot credentials') {
        c.select()
    }

    f.entry(field: 'roomIds', title: 'Room IDs') {
        f.textbox()
    }

}
